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
    "order" bigint,
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

-- Table: public.user_category

-- DROP TABLE IF EXISTS public.user_category;

CREATE TABLE IF NOT EXISTS public.user_category
(
    user_id bigint NOT NULL,
    category_id bigint NOT NULL,
    CONSTRAINT user_category_pkey PRIMARY KEY (user_id, category_id)
    )

    TABLESPACE pg_default;

ALTER TABLE IF EXISTS public.user_category
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

-- public.page definition
-- Static pages, published at /pages/<key>

CREATE TABLE public.page (
	id bigserial NOT NULL,
	"key" varchar(80) NOT NULL,
	title varchar(200) NOT NULL,
	"content" text NULL,
	creator varchar NULL,
	last_modified_by varchar NULL,
	last_modified timestamp NOT NULL DEFAULT now(),
	created timestamp NOT NULL DEFAULT now(),
	visible bool NULL DEFAULT true,
	meta_keywords varchar NULL,
	meta_description varchar NULL,
	CONSTRAINT page_pk PRIMARY KEY (id),
	CONSTRAINT page_key_un UNIQUE (key)
);

-- public.review_type definition
-- A reusable set of review fields, e.g. "Music album" or "Game". Listed at /reviews/<key>.html

CREATE TABLE public.review_type (
	id bigserial NOT NULL,
	"key" varchar(50) NOT NULL,
	title varchar(100) NOT NULL,
	creator varchar NULL,
	created timestamp NULL,
	last_modified timestamp NULL,
	CONSTRAINT review_type_pk PRIMARY KEY (id),
	CONSTRAINT review_type_key_un UNIQUE (key)
);

ALTER TABLE public.category
	ADD COLUMN review_type bigint NULL REFERENCES public.review_type(id);

-- public.review_field definition

CREATE TABLE public.review_field (
	id bigserial NOT NULL,
	review_type bigint NOT NULL REFERENCES public.review_type(id) ON DELETE CASCADE,
	"key" varchar(50) NOT NULL,
	label varchar(100) NOT NULL,
	field_type varchar(20) NOT NULL,       -- TEXT, NUMBER, RATING, DATE, URL, CHOICE
	min_value numeric NULL,
	max_value numeric NULL,
	choices text NULL,                     -- one choice per line
	required bool NOT NULL DEFAULT false,
	"position" int NOT NULL DEFAULT 0,
	show_in_list bool NOT NULL DEFAULT false,
	filterable bool NOT NULL DEFAULT false,
	active bool NOT NULL DEFAULT true,
	CONSTRAINT review_field_pk PRIMARY KEY (id),
	CONSTRAINT review_field_key_un UNIQUE (review_type, key)
);

-- public.news_field_value definition

CREATE TABLE public.news_field_value (
	news_id bigint NOT NULL REFERENCES public.news(id) ON DELETE CASCADE,
	field_id bigint NOT NULL REFERENCES public.review_field(id) ON DELETE CASCADE,
	value_text varchar(1000) NULL,
	value_number numeric NULL,
	CONSTRAINT news_field_value_pk PRIMARY KEY (news_id, field_id)
);
CREATE INDEX news_field_value_number_idx ON public.news_field_value (field_id, value_number);
CREATE INDEX news_field_value_text_idx ON public.news_field_value (field_id, lower(value_text));

-- public.navigation_item definition
-- Default entries are created by the application on first start.

CREATE TABLE public.navigation_item (
	id bigserial NOT NULL,
	menu varchar(20) NOT NULL,             -- HEADER, SIDEBAR
	parent bigint NULL REFERENCES public.navigation_item(id) ON DELETE CASCADE,
	"position" int NOT NULL DEFAULT 0,
	label varchar(100) NULL,
	"type" varchar(20) NOT NULL,           -- PAGE, CATEGORY, CATEGORY_TREE, NEWS_INDEX, REVIEW_INDEX, URL
	page_id bigint NULL REFERENCES public.page(id) ON DELETE CASCADE,
	category_id bigint NULL REFERENCES public.category(id) ON DELETE CASCADE,
	review_type_id bigint NULL REFERENCES public.review_type(id) ON DELETE CASCADE,
	url varchar(500) NULL,
	visible bool NOT NULL DEFAULT true,
	new_tab bool NOT NULL DEFAULT false,
	auto_managed bool NOT NULL DEFAULT false, -- created by the category sync, may be moved by it
	creator varchar NULL,
	created timestamp NULL,
	last_modified timestamp NULL,
	CONSTRAINT navigation_item_pk PRIMARY KEY (id)
);