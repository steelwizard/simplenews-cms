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

* **Categories:** Manage your categories and assign them to users

* **Users:** Manage user accounts

* **Upload Files:** Upload Files for your articles

* **My Profile:** Edit your own profile

## Roles

* **Registered:** New user, cannot do anything yet
* **User:** Can create posts in assigned categories. Can edit own posts.
* **Editor** Can create posts and edit everyone's posts in assigned categories.
* **Admin** Can create and edit all posts in all categories. Can manage system settings.