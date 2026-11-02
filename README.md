# TODO API

Requires Java 17+ and PostgreSQL with an existing `demo` database.

```sh
./mvnw spring-boot:run
# If PostgreSQL requires a password:
DB_PASSWORD=your-password ./mvnw spring-boot:run
```

Defaults: `jdbc:postgresql://localhost:5432/demo`, user `ravisankarpabbati`, empty password.
Override with `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. Hibernate creates/updates the
`todos` table on startup (`ddl-auto=update`, suitable for this local demo).

| Method | Path | Result |
| --- | --- | --- |
| POST | `/api/todos` | Create; 201 and Location header |
| GET | `/api/todos` | List ordered by ID |
| GET | `/api/todos/{id}` | Fetch one |
| PUT | `/api/todos/{id}` | Replace title, description, completion status |
| DELETE | `/api/todos/{id}` | Delete; 204 |

```sh
curl -i http://localhost:8080/api/todos \
  -H 'Content-Type: application/json' \
  -d '{"title":"Buy milk","description":"Two bottles","completed":false}'

curl -X PUT http://localhost:8080/api/todos/1 \
  -H 'Content-Type: application/json' \
  -d '{"title":"Buy milk","description":"Two bottles","completed":true}'
```

Title is required, nonblank, and limited to 200 characters. Description is optional
and limited to 2000 characters. Completion defaults to false. PUT replaces the whole
resource: omitted description becomes null and omitted completion becomes false.
Invalid requests return 400; missing TODOs return 404.

The controller handles HTTP, the transactional service handles operations, and the
Spring Data repository handles JPA persistence. Request/response records keep the
HTTP contract separate from the entity. Existing `/hello` examples remain available.

Run `./mvnw test`. Tests use an isolated H2 database and do not modify local PostgreSQL.
