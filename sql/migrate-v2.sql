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
