# simplenews-cms
CMS based on Spring Boot

Simplenews is a simple CMS for oldschool websites and blogs.
It uses standard HTML for formatting and allows to create different
categories of Posts.
The simplenews CMS uses the Spring Boot Framework, Thymeleaf and PostgreSQL.

## Getting started

Clone the repository:

`git clone https://github.com/steelwizard/simplenews-cms.git`

### Running in docker

The fastest way to get started is using running the application in docker.
Simply run the `build-to-docker` script. The application may restart a few times
while the database is initializing. Give it a moment to finish.

### Running directly

If you want to run the application directly you need to first set up your local database.
Use the `/sql/news-init.sql` file to create the database and set up your database url in
`application.properties`.

To run the application directly in through maven use the following command:

`./mvnw spring-boot:run`

### Building an executable .jar file

You can build an executable .jar with
`./mvnw clean package`.

Run the application with `java -jar target/cms-0.0.4-SNAPSHOT.jar`.

Either way, the application should now be reachable at `localhost:8080`

### Changing the admin password before running the application:

For security reasons it might be better not to use the standard password in a production
scenario. You can use the script `sql\bcrypt-encode.py` to encode your new password.

Go to `sql/news-init.sql` and find the following line

`VALUES(0, 'admin', '$2a$10$R6oQRfhigKPu4zEIpLnunewZpt3aiA.DHdm1U6GgjFd/kjibXoQ92', 'Admin', 0, 0, false, now(), now(), 'ADMIN', 'admin@example.com', true);`

Change the password field ("$2a$10[...]") to your new encoded password.

### Configuration

The `application.properties` file contains some important settings.

A database must be configured for the application to work:

`spring.datasource.url=jdbc:postgresql://simplenews-cms-data-1:5432/news # database url
spring.datasource.username=postgres # database user
spring.datasource.password=postgres # database password
spring.jpa.properties.hibernate.default_schema=public # database schema`

You also to provide a data directory:

`cms.data.path = /opt/cms`

## Admin control panel
navigate to `localhost:8080/_private/admin` to access the ACP.
A standard user is generated with username and password `admin`.

It has the following sections:

* **Posts:** create and edit posts

* **Pages:** create static pages, published at `/pages/<key>.html` (editors and admins)

* **Navigation:** arrange the header and sidebar menus with links to pages, categories,
  review lists, the category tree or any address (admins)

* **Categories:** Manage your categories and assign them to users. A category of type `REVIEW`
  uses a review type, and its posts get that review type's extra fields.

* **Reviews:** define review types (e.g. "Music album", "Game") and their adjustable fields.
  Reviews of a type are listed at `/reviews/<key>.html`, optionally sorted by rating (admins)

* **Users:** Manage user accounts

* **Upload Files:** Upload Files for your articles

* **My Profile:** Edit your own profile

## Roles

* **Registered:** New user, cannot do anything yet
* **User:** Can create posts in assigned categories. Can edit own posts.
* **Editor** Can create posts and edit everyone's posts in assigned categories.
* **Admin** Can create and edit all posts in all categories. Can manage system settings.

## Article comments

Comments are off until `cms.phpbb.enabled` is `true`. The site does not talk to the forum while they are off, and a published article does not get a thread until someone opens it.

With comments on, an article shows **Discuss this article**. The first visit creates one topic in the forum and sends the reader there. Later visits go to that same topic. The reply count is whatever the forum last reported. If the forum or its database is down, the article still loads; only that discussion link answers that the forum is unavailable.

The forum is served by the same address as the site, under `/forum` (http://localhost:8080/forum). It is not required for the rest of the site.

One-time setup after `docker compose up`:

1. Open http://localhost:8080/forum and sign in as `admin` / `adminadmin`. Change that password.
2. Rename the first forum to something like "Article discussion", or create one. Its id is in the address bar (`f=2` on a fresh board). Put that id in `cms.phpbb.forum-id`.
3. In `application.properties`, set `cms.phpbb.enabled=true`. The dev keys `dev-api-key` and `dev-webhook-key` already match the forum container. `cms.phpbb.base-url` is the public `/forum` address, and `cms.phpbb.site-url` is the public address of this site.
4. Restart the CMS.

The forum container creates the user `simplenews` and enables the SimpleNews discussion extension on startup. That user writes the opening post of each article topic. Leave guest posting off.

An existing database volume will not create the `phpbb` database by itself. The forum container creates it on startup when Postgres is up. If that database is missing, only the forum keeps retrying.
