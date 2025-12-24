-- public.category definition

-- Drop table

-- DROP TABLE public.category;

CREATE TABLE public.category (
	id bigserial NOT NULL,
	"name" varchar NOT NULL,
	title varchar NULL,
	"options" json NULL,
	"type" varchar NOT NULL,
	creator varchar NULL,
    last_modified_by varchar NULL,
	created timestamp NULL,
	last_modified timestamp NULL,
	visible bool NOT NULL DEFAULT true,
	parent bigint NULL,
    meta_keywords varchar COLLATE pg_catalog."default",
    meta_description varchar COLLATE pg_catalog."default",
	CONSTRAINT category_pk PRIMARY KEY (id)
);


-- public."user" definition

-- Drop table

-- DROP TABLE public."user";

CREATE TABLE public."user" (
	id bigserial NOT NULL,
	username varchar NOT NULL,
	"password" varchar NOT NULL,
	display_name varchar NULL,
	alg int8 NULL DEFAULT 0,
	wrong_password_count int8 NULL DEFAULT 0,
	password_change_required bool NULL DEFAULT false,
	last_modified timestamp NOT NULL DEFAULT now(),
	created timestamp NOT NULL DEFAULT now(),
	"role" varchar NULL,
	email varchar NULL,
	active bool NULL,
	CONSTRAINT user_pk PRIMARY KEY (id),
	CONSTRAINT user_un UNIQUE (username)
);


-- public.news definition

-- Drop table

-- DROP TABLE public.news;

CREATE TABLE public.news (
	id bigserial NOT NULL,
	"key" varchar NOT NULL,
	title varchar NOT NULL,
	"content" text NULL,
	last_modified timestamp NOT NULL DEFAULT now(),
	tags _varchar NULL,
	category int8 NULL,
	creator varchar NULL,
    last_modified_by varchar NULL,
	created timestamp NOT NULL DEFAULT now(),
	visible bool NULL,
	"options" json NULL,
    meta_keywords varchar NULL,
    meta_description varchar NULL,
	CONSTRAINT news_pk PRIMARY KEY (id),
	CONSTRAINT news_un UNIQUE (key),
	CONSTRAINT news_fk FOREIGN KEY (category) REFERENCES public.category(id)
);

-- admin:admin example user, should be changed so there is no insecure default password
INSERT INTO public."user"
(id, username, "password", display_name, alg, wrong_password_count, password_change_required, last_modified, created, "role", email, active)
VALUES(0, 'admin', '$2a$10$R6oQRfhigKPu4zEIpLnunewZpt3aiA.DHdm1U6GgjFd/kjibXoQ92', 'Admin', 0, 0, false, now(), now(), 'ADMIN', 'admin@example.com', true);

INSERT INTO public.category
(id, "name", title, "options", "type", creator, created, last_modified, visible, parent)
VALUES(0, 'news', 'News', NULL, 'NEWS', NULL, now(), now(), true, NULL);

INSERT INTO public.news
(id, "key", title, "content", last_modified, tags, category, creator, created, visible, "options")
VALUES(9, 'test', 'Welcome!', 'Welcome to a brand new website!', now(), NULL, 0, 'admin', now(), true, NULL);

-- Table: public.upload_file

-- DROP TABLE IF EXISTS public.upload_file;

CREATE TABLE IF NOT EXISTS public.upload_file
(

    id bigserial NOT NULL,
    hash varchar COLLATE pg_catalog."default",
    extension varchar COLLATE pg_catalog."default",
    filename varchar COLLATE pg_catalog."default",
    created timestamp without time zone DEFAULT now(),
    creator varchar COLLATE pg_catalog."default",
    CONSTRAINT upload_file_pkey PRIMARY KEY (id)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.upload_file
    OWNER to postgres;

-- Table: public.site_setting

-- DROP TABLE IF EXISTS public.site_setting;

CREATE TABLE IF NOT EXISTS public.site_setting
(
    key varchar COLLATE pg_catalog."default" NOT NULL,
    value varchar COLLATE pg_catalog."default",
    CONSTRAINT "site-settings_pkey" PRIMARY KEY (key)
)

TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.site_setting
    OWNER to postgres;

INSERT INTO site_setting (key, value)
	VALUES
	('site.meta.keywords', 'blog, news'),
	('site.meta.description','Welcome to my website!'),
	('site.title','My Site');