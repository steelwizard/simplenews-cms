# simplenews-cms
CMS based on Spring Boot

Simplenews is a simple CMS for oldschool websites and blogs.
It uses standard HTML for formatting and allows to create different
categories of Posts.
The simplenews CMS uses the Spring Boot Framework, Thymeleaf and PostgreSQL.
It needs Java 25. The public site uses a dark theme.

## Getting started

Clone the repository:

`git clone https://github.com/steelwizard/simplenews-cms.git`

### Running in docker

The fastest way to get started is running the application in docker.
Simply run the `build-to-docker` script (`build-to-docker.bat` on Windows).
That starts the site, PostgreSQL, Varnish and the forum. The application may restart a few times
while the database is initializing. Give it a moment to finish.

Port 8080 is Varnish. It caches public CSS, JavaScript and JSON.
HTML pages are not cached, because each view is counted.

The first start adds a few examples: a music review, a game review, a download
post and an About us page. They are ordinary content. Edit or delete them like anything else.

### Running directly

Running the application directly needs Java 25 and a local database.
Use the `/sql/news-init.sql` file to create the database and set your database url in
`application.properties`. The file in the project root is the one Docker mounts, and
Spring Boot also reads it when you start from that directory. For a local database
the url is `jdbc:postgresql://localhost:5432/news`.

To run the application directly through maven use the following command:

`./mvnw spring-boot:run`

On Windows that is `mvnw.cmd spring-boot:run`.

### Building an executable .jar file

You can build an executable .jar with
`./mvnw clean package`
(`mvnw.cmd clean package` on Windows).

Run the application with `java -jar target/cms-0.0.4-SNAPSHOT.jar`.

Either way, the application should now be reachable at `localhost:8080`

### Changing the admin password before running the application:

For security reasons it might be better not to use the standard password in a production
scenario. You can use the script `sql\bcrypt-encode.py` to encode your new password.

Go to `sql/news-init.sql` and find the following line

`VALUES(0, 'admin', '$2a$10$R6oQRfhigKPu4zEIpLnunewZpt3aiA.DHdm1U6GgjFd/kjibXoQ92', 'Admin', 0, 0, false, now(), now(), 'ADMIN', 'admin@example.com', true);`

Change the password field ("$2a$10[...]") to your new encoded password.

### Configuration

The `application.properties` file in the project root contains some important settings.
Docker mounts it over the defaults packaged in `src/main/resources/application.properties`.

A database must be configured for the application to work:

`spring.datasource.url=jdbc:postgresql://simplenews-cms-data-1:5432/news # database url
spring.datasource.username=postgres # database user
spring.datasource.password=postgres # database password
spring.jpa.properties.hibernate.default_schema=public # database schema`

You also need to provide a data directory:

`cms.data.path = /opt/cms`

In Docker that path is the `_data/cms` folder. For a direct run, point it at a folder on your machine.

## The site

The front page is `/index.html`. A post is at `/{id}-{key}.html` (and `/{id}.html`).
A category is `/category/<name>.html`. On a listing, a long post is cut to 300 characters,
with a link to the full article.

The same addresses with `.json` instead of `.html` return the data.
Pages are at `/pages/<key>.html`. Review lists are at `/reviews/<key>.html`.
Old addresses under `/news/` redirect to the new ones.

Every successful public HTML page counts one click. Admin pages, files and JSON do not.
The total is a plain number at `/clicks/<secret>`. The secret is created on the first
click and stored in the `secret` column of `click_counter`.

## Admin control panel
navigate to `localhost:8080/_private/admin` to access the ACP.
A standard user is generated with username and password `admin`.

It has the following sections:

* **Posts:** create and edit posts. The editor is TinyMCE and the stored text is HTML.
  A post can have files. Images, video and audio are shown in the article. Other files
  are download links. Preview shows the page before it is saved.

* **Pages:** create static pages, published at `/pages/<key>.html` (editors and admins)

* **Navigation:** arrange the header and sidebar menus with links to pages, categories,
  review lists, the category tree or any address (admins)

* **Categories:** Manage your categories and assign them to users. A category of type `REVIEW`
  uses a review type, and its posts get that review type's extra fields. A category of type
  `DOWNLOAD` is for posts that are mainly files.

* **Reviews:** define review types (e.g. "Music album", "Game") and their adjustable fields.
  A field can be text, a number, a rating, a percent, a date, a URL or a choice.
  Reviews of a type are listed at `/reviews/<key>.html`, optionally sorted by rating (admins)

* **Users:** Manage user accounts

* **Upload Files:** Upload Files for your articles

* **My Profile:** Edit your own profile

## Roles

* **Registered:** New user, cannot do anything yet
* **User:** Can create posts in assigned categories. Can edit own posts.
* **Editor** Can create posts and edit everyone's posts in assigned categories. Can edit pages.
* **Admin** Can create and edit all posts in all categories. Can manage system settings.

## Article comments

Comments are off until `cms.phpbb.enabled` is `true`. The site does not talk to the forum while they are off, and a published article does not get a thread until someone opens it.

With comments on, an article shows **Discuss this article**. The first visit creates one topic in the forum and sends the reader there. Later visits go to that same topic. The reply count is whatever the forum last reported. If the forum or its database is down, the article still loads; only that discussion link answers that the forum is unavailable.

The forum is served by the same address as the site, under `/forum` (http://localhost:8080/forum). It is not required for the rest of the site.

One-time setup after `docker compose up`:

1. Open http://localhost:8080/forum and sign in as `admin` / `adminadmin`. Change that password.
2. Rename the first forum to something like "Article discussion", or create one. Its id is in the address bar (`f=2` on a fresh board). Put that id in `cms.phpbb.forum-id`.
3. In `application.properties`, set `cms.phpbb.enabled=true`. The project-root file already has this for Docker. The packaged default is `false`. The dev keys `dev-api-key` and `dev-webhook-key` already match the forum container. `cms.phpbb.base-url` is the public `/forum` address, and `cms.phpbb.site-url` is the public address of this site.
4. Restart the CMS.

The forum container creates the user `simplenews` and enables the SimpleNews discussion extension on startup. That user writes the opening post of each article topic. Leave guest posting off.

An existing database volume will not create the `phpbb` database by itself. The forum container creates it on startup when Postgres is up. If that database is missing, only the forum keeps retrying.
