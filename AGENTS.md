# AGENTS.md — Wau

Pet-boarding app (guardería canina). Two halves, no monorepo tooling, no CI/lint configs.

- `backend/` — Spring Boot API (Java 21, Maven wrapper). Layers: `controller/` → `service/` → `repository/` + `entity/`, with `dto/`, `security/`, `config/`.
- `frontend/` — vanilla static HTML/CSS/JS. No `package.json`, no bundler. `js/` and `css/` mirror page structure: `auth/`, `dueno/`, `guarderia/`.

## Prerequisites

- Java 21, Maven wrapper included (use `mvnw.cmd` on Windows, `./mvnw` on Unix).
- Docker for Postgres. Backend will not start without it.

## Run

```powershell
docker compose up -d                    # from repo root: postgres:16, wau_db / postgres / password123 on localhost:5432
cd backend; .\mvnw.cmd spring-boot:run  # API on http://localhost:8080
```

Frontend has no dev server: open `frontend/index.html` directly or serve it (`npx serve frontend`). CORS is wide-open (`*`), so either works.

## Test

```powershell
cd backend; .\mvnw.cmd test                              # only test today: contextLoads — needs DB up
.\mvnw.cmd -Dtest=BackendApplicationTests test            # single class
.\mvnw.cmd -Dtest=BackendApplicationTests#contextLoads test  # single method
```

## Conventions that matter

- DB schema is `spring.jpa.hibernate.ddl-auto=update` (`backend/src/main/resources/application.properties`). No migrations — changing an `@Entity` changes the schema on next boot.
- Auth: `POST /api/auth/registro`, `POST /api/auth/login` are the only public routes (`SecurityConfig`). Everything else needs `Authorization: Bearer <token>`.
- Roles are `ROLE_DUENO` / `ROLE_GUARDERIA`. Login (`frontend/js/auth/login.js`) stores `token` + `userSession` (`{id,email,rol}`) in `localStorage` and redirects by role. Authenticated pages read/clear those same keys — keep the key names.
- Backend services resolve the user from `Authentication.getName()` (JWT subject = email). Don't pass user email from the client.
- API base URL is hardcoded as `http://localhost:8080` in every file under `frontend/js/`. There is no shared config — a host/port change means editing all of them.
- JWT secret + 24h expiry are hardcoded in `security/JwtService.java`. No env loading exists.
- Seed user `dueno@wau.com` / `123456` is created on every boot by `DataInitializer.java` if the email is absent. Useful for manual login testing.
- Controllers return `400` with a plain-text message on auth failures (`AuthController`) — frontend reads `response.text()`, not JSON, on error.
