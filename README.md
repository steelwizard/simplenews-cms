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

Either way, the application should now be reachable at `localhost:8080`

## Admin control panel
navigate to `localhost:8080/_private/admin` to access the ACP.
A standard user is generated with username and password admin.

It has the following sections:

* **Posts:** create and edit posts.

* **Categories:** Manage your categories and assign them to users.

* **Users:** Manage user accounts

* **Upload Files:** Upload Files for your articles

* **My Profile:** Edit your own profile

## Roles

* **Registered:** New user, cannot do anything yet
* **User:** Can create posts in assigned categories. Can edit own posts.
* **Editor** Can create posts and edit everyone's posts in assigned categories.
* **Admin** Can create and edit all posts in all categories. Can manage system settings.