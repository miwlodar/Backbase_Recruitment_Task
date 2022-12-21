# Users Manager
Application created for recruitment purposes for Backbase
by Michal Wlodarczyk (2022)

## Description
REST API app for managing users

### Technologies used
- Java 11
- Spring
- Gradle
- IntelliJ IDEA
- MySQL and MySQL Workbench
- JUnit and Mockito (with H2 DB)

### How to run and use the app
1. Create the seed database - run SQL script included in the project (sql-script/UsersDatabase.sql) with MySQL Workbench (File -> Open SQL Script) - requires MySQL to be installed first
2. Run the main app (src/main/java/io/github/miwlodar/MainApplication.java) with IntelliJ using JDK 11
3. Enjoy the Users Manager App equipped with all CRUD operations using REST API

### App's REST API endpoints
| Method| Url | Action |
|-------|-----|--------|
| GET   | api/users  | retrieve all users with their IDs (with Pageable interface - pagination and sorting enabled) |
| GET   | api/users/{id} | retrieve user by ID |
| GET   | api/users-by-lastname  | retrieve a list of users with given last name (without IDs) by adding "?lastName={lastName}" to URL |
| GET   | api/users-firstnames-by-lastname  | retrieve a list of users' first names with given last name (without IDs) by adding "?lastName={lastName}" to URL|
| POST  | api/users  | create a new user |
| PUT   | api/users/{id} | update an existing user (requires providing all the user's fields) |
| PATCH | api/users/{id} | update an existing user (doesn't require providing all the user's fields) |
| DELETE| api/users/{id} | delete user by ID |

### Usage examples
Usage examples with the responses based on the seed database records

1. Get all users
    ```shell script
    curl -X GET http://localhost:8080/api/users
    ```
   Response:
```shell script
[
{
"id": 1,
"first_name": "John",
"last_name": "Doe"
},
{
"id": 2,
"first_name": "Jane",
"last_name": "Doe"
},
{
"id": 3,
"first_name": "John",
"last_name": "Smith"
},
{
"id": 4,
"first_name": "Jane",
"last_name": "Smith"
},
{
"id": 5,
"first_name": "Jan",
"last_name": "Kowalski"
}
]
```

2. Get user with ID 1
    ```shell script
    curl -X GET http://localhost:8080/api/users/1
    ```
   Response:
```shell script
{
"id": 1,
"first_name": "John",
"last_name": "Doe"
}
```

3. Get users with last name Doe
    ```shell script
    curl -X GET http://localhost:8080/api/users-by-lastname?lastName=doe
    ```
   Response:
```shell script
[
{
"first_name": "John",
"last_name": "Doe"
},
{
"first_name": "Jane",
"last_name": "Doe"
}
]
```

4. Get users' first names with last name Doe
    ```shell script
    curl -X GET http://localhost:8080/api/users-firstnames-by-lastname?lastName=doe
    ```
   Response:
```shell script
[
"John",
"Jane"
]
```

5. Create user with specified first name and last name
    ```shell script
    curl -X POST \
      http://localhost:8080/api/users \
      -H 'Content-Type: application/json' \
      -d '{
        "first_name": "Bruce",
        "last_name": "Lee"
    }'
    ```
   Response:
```shell script
{
    "id": 6,
    "first_name": "Bruce",
    "last_name": "Lee"
}
```

6. Update user with ID 1
    ```shell script
    curl -X PUT \
      http://localhost:8080/api/users/1 \
      -H 'Content-Type: application/json' \
      -d '{
        "first_name": "Johnny",
        "last_name": "Doe"
    }'
    ```
   Response:
```shell script
{
    "id": 1,
    "first_name": "Johnny",
    "last_name": "Doe"
}
```

7. Patch user with ID 2 (i.e. update by providing only selected fields to be modified)
    ```shell script
    curl -X PATCH \
      http://localhost:8080/api/users/2 \
      -H 'Content-Type: application/json' \
      -d '{
        "first_name": "Judy"
    }'
    ```
   Response:
```shell script
{
    "id": 2,
    "first_name": "Judy",
    "last_name": "Doe"
}
```

8. Delete user with ID 5
    ```shell script
    curl -X DELETE http://localhost:8080/api/users/5
    ```
   Response:
```shell script
200 OK
```
