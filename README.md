# OTodo

OTodo is a full-stack todo application with an Ionic/Angular PWA client and a Spring Boot backend. The client stores todos locally with Dexie/IndexedDB and syncs them with the backend when online.

## Stack

- Client: Angular 22, Ionic 8, Dexie, RxJS, date-fns
- Server: Spring Boot 4, Spring Security, jOOQ, Flyway, PostgreSQL
- Tests: JUnit 5, Testcontainers, GreenMail, Spring Boot test support

## Requirements

- Node.js and npm compatible with Angular 22
- Java 25 or newer
- Docker for the local PostgreSQL 18 Alpine container and backend integration tests

## Client

```bash
cd client
npm install
npm start
```

The dev server uses `proxy.conf.json` to forward `/be` requests to the backend at `http://localhost:8080`.

Useful client commands:

```bash
npm run lint
npm run build
npm run serve-dist
```

`npm run build` updates `src/environments/environment.prod.ts`, collects used Ionicons, builds the production app into `client/dist/app`, and runs `bread-compressor` on the output.

## Server

Start PostgreSQL:

```bash
cd server
docker compose up -d
```

The default backend configuration uses:

- JDBC URL: `jdbc:postgresql://localhost:5432/otodo`
- Database: `otodo`
- User/password: `postgres` / `postgres`
- Server port: `8080`

Run the backend:

```bash
cd server
.\mvnw.cmd spring-boot:run
```

Run backend tests:

```bash
cd server
.\mvnw.cmd test
```

## Development Notes

- API routes are served under `/be`.
- The client application URL used by backend emails is configured as `app.url=http://localhost:8100`.
- Local email delivery expects an SMTP server on `localhost:25`; tests use GreenMail.
- jOOQ sources are checked into `server/src/main/java/ch/rasc/otodo/db`. Regenerate them with the provided `generatejooq*.bat` scripts when the schema changes.

## Verification

Current health checks:

```bash
cd client && npm run lint
cd client && npm run build
cd server && .\mvnw.cmd test
```

