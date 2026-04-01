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
    CONSTRAINT key_un UNIQUE (name),
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


-- public.content definition (base table for all content types)

CREATE TABLE public.content (
	id bigserial NOT NULL,
	dtype varchar(31) NOT NULL DEFAULT 'News',
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
	preview character varying NULL,
	parent_id bigint NULL,
	CONSTRAINT content_pk PRIMARY KEY (id),
	CONSTRAINT content_key_un UNIQUE (key),
	CONSTRAINT content_category_fk FOREIGN KEY (category) REFERENCES public.category(id),
	CONSTRAINT content_parent_fk FOREIGN KEY (parent_id) REFERENCES public.content(id)
);

-- news_content: child table for plain news articles (no extra columns)
CREATE TABLE public.news_content (
	id bigint NOT NULL,
	CONSTRAINT news_content_pk PRIMARY KEY (id),
	CONSTRAINT news_content_fk FOREIGN KEY (id) REFERENCES public.content(id)
);

-- review_content: child table for reviews with score and customizable fields
CREATE TABLE public.review_content (
	id bigint NOT NULL,
	score numeric(4,2) NULL,
	CONSTRAINT review_content_pk PRIMARY KEY (id),
	CONSTRAINT review_content_fk FOREIGN KEY (id) REFERENCES public.content(id)
);

-- article_content: child table for static pages in a tree (parent_id lives on content)
CREATE TABLE public.article_content (
	id bigint NOT NULL,
	order_index int NULL DEFAULT 0,
	CONSTRAINT article_content_pk PRIMARY KEY (id),
	CONSTRAINT article_content_fk FOREIGN KEY (id) REFERENCES public.content(id)
);

-- review_field_definition: defines which custom fields a review category uses
CREATE TABLE public.review_field_definition (
	id bigserial NOT NULL,
	category_id bigint NOT NULL,
	field_key varchar NOT NULL,
	field_label varchar NOT NULL,
	field_type varchar NOT NULL,
	required bool NOT NULL DEFAULT false,
	order_index int NOT NULL DEFAULT 0,
	CONSTRAINT review_field_def_pk PRIMARY KEY (id),
	CONSTRAINT review_field_def_category_fk FOREIGN KEY (category_id) REFERENCES public.category(id)
);

-- review_field_option: allowed values for SELECT-type fields
CREATE TABLE public.review_field_option (
	id bigserial NOT NULL,
	field_definition_id bigint NOT NULL,
	value varchar NOT NULL,
	order_index int NOT NULL DEFAULT 0,
	CONSTRAINT review_field_option_pk PRIMARY KEY (id),
	CONSTRAINT review_field_option_def_fk FOREIGN KEY (field_definition_id) REFERENCES public.review_field_definition(id)
);

-- review_field_value: custom field values stored per review
CREATE TABLE public.review_field_value (
	id bigserial NOT NULL,
	review_content_id bigint NOT NULL,
	field_definition_id bigint NOT NULL,
	value varchar NULL,
	CONSTRAINT review_field_value_pk PRIMARY KEY (id),
	CONSTRAINT review_field_value_content_fk FOREIGN KEY (review_content_id) REFERENCES public.review_content(id),
	CONSTRAINT review_field_value_def_fk FOREIGN KEY (field_definition_id) REFERENCES public.review_field_definition(id)
);

-- nav_tree: named navigation trees (e.g. "main", "footer")
CREATE TABLE public.nav_tree (
	id bigserial NOT NULL,
	handle varchar NOT NULL,
	title varchar NULL,
	CONSTRAINT nav_tree_pk PRIMARY KEY (id),
	CONSTRAINT nav_tree_handle_un UNIQUE (handle)
);

-- nav_node: nodes within a navigation tree
CREATE TABLE public.nav_node (
	id bigserial NOT NULL,
	tree_id bigint NOT NULL,
	parent_id bigint NULL,
	label varchar NOT NULL,
	url varchar NULL,
	ref_type varchar NULL,
	ref_id bigint NULL,
	visible bool NOT NULL DEFAULT true,
	open_in_new_tab bool NOT NULL DEFAULT false,
	order_index int NOT NULL DEFAULT 0,
	CONSTRAINT nav_node_pk PRIMARY KEY (id),
	CONSTRAINT nav_node_tree_fk FOREIGN KEY (tree_id) REFERENCES public.nav_tree(id),
	CONSTRAINT nav_node_parent_fk FOREIGN KEY (parent_id) REFERENCES public.nav_node(id)
);

-- admin:admin example user, should be changed so there is no insecure default password
INSERT INTO public."user"
(id, username, "password", display_name, alg, wrong_password_count, password_change_required, last_modified, created, "role", email, active)
VALUES(0, 'admin', '$2a$10$R6oQRfhigKPu4zEIpLnunewZpt3aiA.DHdm1U6GgjFd/kjibXoQ92', 'Admin', 0, 0, false, now(), now(), 'ADMIN', 'admin@example.com', true);

INSERT INTO public.category
(id, "name", title, "options", "type", creator, created, last_modified, visible, parent)
VALUES(0, 'news', 'News', NULL, 'NEWS', NULL, now(), now(), true, NULL);

INSERT INTO public.content
(id, dtype, "key", title, "content", last_modified, category, creator, created, visible)
VALUES(9, 'News', 'test', 'Welcome!', 'Welcome to a brand new website!', now(), 0, 'admin', now(), true);

INSERT INTO public.news_content (id) VALUES (9);

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



------ New stuff
-- Migration: news table -> content/news_content inheritance hierarchy
-- Also adds: review_content, article_content, review_field_*, nav_tree, nav_node

-- 1. Add unique constraint to category if missing
ALTER TABLE public.category
    ADD CONSTRAINT IF NOT EXISTS key_un UNIQUE (name);

-- 2. Create base content table
CREATE TABLE IF NOT EXISTS public.content (
    id bigserial NOT NULL,
    dtype varchar(31) NOT NULL DEFAULT 'News',
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
    preview character varying NULL,
    parent_id bigint NULL,
    CONSTRAINT content_pk PRIMARY KEY (id),
    CONSTRAINT content_key_un UNIQUE (key),
    CONSTRAINT content_category_fk FOREIGN KEY (category) REFERENCES public.category(id),
    CONSTRAINT content_parent_fk FOREIGN KEY (parent_id) REFERENCES public.content(id)
);

-- 3. Migrate existing news rows into content
INSERT INTO public.content (id, dtype, "key", title, "content", last_modified, tags, category, creator, last_modified_by, created, visible, "options", meta_keywords, meta_description)
SELECT id, 'News', "key", title, "content", last_modified, tags, category, creator, last_modified_by, created, visible, "options", meta_keywords, meta_description
FROM public.news
ON CONFLICT DO NOTHING;

-- Keep the content id sequence ahead of migrated rows
SELECT setval('public.content_id_seq', GREATEST((SELECT MAX(id) FROM public.content), 1));

-- 4. Create news_content child table
CREATE TABLE IF NOT EXISTS public.news_content (
    id bigint NOT NULL,
    CONSTRAINT news_content_pk PRIMARY KEY (id),
    CONSTRAINT news_content_fk FOREIGN KEY (id) REFERENCES public.content(id)
);

-- Populate news_content for every migrated News row
INSERT INTO public.news_content (id)
SELECT id FROM public.content WHERE dtype = 'News'
ON CONFLICT DO NOTHING;

-- 5. Create review_content child table
CREATE TABLE IF NOT EXISTS public.review_content (
    id bigint NOT NULL,
    score numeric(4,2) NULL,
    CONSTRAINT review_content_pk PRIMARY KEY (id),
    CONSTRAINT review_content_fk FOREIGN KEY (id) REFERENCES public.content(id)
);

-- 6. Create article_content child table
CREATE TABLE IF NOT EXISTS public.article_content (
    id bigint NOT NULL,
    order_index int NULL DEFAULT 0,
    CONSTRAINT article_content_pk PRIMARY KEY (id),
    CONSTRAINT article_content_fk FOREIGN KEY (id) REFERENCES public.content(id)
);

-- 7. Create review_field_definition
CREATE TABLE IF NOT EXISTS public.review_field_definition (
    id bigserial NOT NULL,
    category_id bigint NOT NULL,
    field_key varchar NOT NULL,
    field_label varchar NOT NULL,
    field_type varchar NOT NULL,
    required bool NOT NULL DEFAULT false,
    order_index int NOT NULL DEFAULT 0,
    CONSTRAINT review_field_def_pk PRIMARY KEY (id),
    CONSTRAINT review_field_def_category_fk FOREIGN KEY (category_id) REFERENCES public.category(id)
);

-- 8. Create review_field_option
CREATE TABLE IF NOT EXISTS public.review_field_option (
    id bigserial NOT NULL,
    field_definition_id bigint NOT NULL,
    value varchar NOT NULL,
    order_index int NOT NULL DEFAULT 0,
    CONSTRAINT review_field_option_pk PRIMARY KEY (id),
    CONSTRAINT review_field_option_def_fk FOREIGN KEY (field_definition_id) REFERENCES public.review_field_definition(id)
);

-- 9. Create review_field_value
CREATE TABLE IF NOT EXISTS public.review_field_value (
    id bigserial NOT NULL,
    review_content_id bigint NOT NULL,
    field_definition_id bigint NOT NULL,
    value varchar NULL,
    CONSTRAINT review_field_value_pk PRIMARY KEY (id),
    CONSTRAINT review_field_value_content_fk FOREIGN KEY (review_content_id) REFERENCES public.review_content(id),
    CONSTRAINT review_field_value_def_fk FOREIGN KEY (field_definition_id) REFERENCES public.review_field_definition(id)
);

-- 10. Create nav_tree
CREATE TABLE IF NOT EXISTS public.nav_tree (
    id bigserial NOT NULL,
    handle varchar NOT NULL,
    title varchar NULL,
    CONSTRAINT nav_tree_pk PRIMARY KEY (id),
    CONSTRAINT nav_tree_handle_un UNIQUE (handle)
);

-- 11. Create nav_node
CREATE TABLE IF NOT EXISTS public.nav_node (
    id bigserial NOT NULL,
    tree_id bigint NOT NULL,
    parent_id bigint NULL,
    label varchar NOT NULL,
    url varchar NULL,
    ref_type varchar NULL,
    ref_id bigint NULL,
    visible bool NOT NULL DEFAULT true,
    open_in_new_tab bool NOT NULL DEFAULT false,
    order_index int NOT NULL DEFAULT 0,
    CONSTRAINT nav_node_pk PRIMARY KEY (id),
    CONSTRAINT nav_node_tree_fk FOREIGN KEY (tree_id) REFERENCES public.nav_tree(id),
    CONSTRAINT nav_node_parent_fk FOREIGN KEY (parent_id) REFERENCES public.nav_node(id)
);
