# AGENTS.md — Wau

Pet-boarding app (guardería canina). Two halves, no monorepo tooling, no CI/lint configs.

- `backend/` — Spring Boot API (Java 21, Maven wrapper). Layers: `controller/` → `service/` → `repository/` + `entity/`, with `dto/`, `security/`, `config/`.
- `frontend/` — vanilla static HTML/CSS/JS. No `package.json`, no bundler. `js/` and `css/` mirror page structure: `auth/`, `dueno/`, `guarderia/`.

## Prerequisites

- Java 21, Maven wrapper included (use `mvnw.cmd` on Windows, `./mvnw` on Unix).
- Docker for Postgres. Backend will not start without it.

## Run

```powershell
docker compose up -d                    # from repo root: app DB (wau_db/postgres@5432) + Evolution WhatsApp stack (API :8082, Manager :3000)
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

## WhatsApp notifications (Evolution API)

Every timeline milestone (`POST /api/estadias/{id}/actividades`, only for `ACTIVA` stages) triggers an asynchronous WhatsApp to the owner's `telefono`: `NotificacionService` (`@Async`, best-effort — failures are logged, never break the API response) → `EvolutionApiService` → Evolution API. There is NO retry queue: if Evolution is down at moment the milestone is created, that notification is lost (check `log.warn` in backend console).

- Setup lives in Docker: `evolution-api` (:8082→8080, v2.3.7) with its own `evolution-postgres` (DB `evolution_db`) + `evolution-redis`, plus `evolution-manager` (:3000→80) for the pairing UI. The app DB (`wau-postgres`, `wau_db`) is untouched and separate.
- Config must stay in sync across files: `evolution.apikey` (`application.properties`) == `AUTHENTICATION_API_KEY` (docker-compose.yml); `evolution.instance` == the instance name in Evolution (`wau-guarderia`).
- Owner phone must be international WITH country code (Arg mobile: `+54 9 <area> <number>`, e.g. `+5493511234567`). `+`, spaces, dashes are stripped; local `0…` / `15…` formats break delivery silently.
- The Manager SPA is served only under `/manager` (use `http://localhost:8082/manager` — embedded in the API — or `http://localhost:3000/manager`). Root `/` 302-redirects there via the mounted nginx.conf (without it React Router shows a bare 404). Login screen asks Server URL + API key.
- `evoapicloud/evolution-manager` upstream images (all tags incl. latest) ship an invalid `gzip_proxied` value and nginx won't start: `docker/evolution-manager/nginx.conf` is mounted via compose to fix it. Re-check if the upstream build is ever updated.

### Daily ops

- End of day: `docker compose stop` (volumes preserved). Back in the morning: `docker compose start` (or `docker compose up -d` after a reboot — containers auto-restart with Docker Desktop thanks to `restart: always`).
- `docker compose down` is safe: containers/network are recreated, all volumes persist and the WhatsApp re-connects by itself. **NEVER `docker compose down -v` on this repo** — it wipes `wau_db` (all app data) and the WhatsApp pairing (forces a fresh QR scan).
- Health check after any restart: `http://localhost:8082/manager` → instance `wau-guarderia` must be `open`. If `close`: Connect button → re-scan QR with the platform phone.
- Do NOT use the instance `Logout` button in Manager nor unlink the device in the phone's Linked Devices list — either invalidates the session (QR re-scan required).
- Volumes that matter (keep them): `wau_postgres_data`, `wau_evolution_postgres_data`, `wau_evolution_redis_data`, `wau_evolution_instances`.

### Disaster recovery (new machine / lost computer)

Git holds code + compose definitions, NOT data. With a fresh clone: `docker compose up -d` → empty DBs (app seed regenerates automatically; Prisma creates Evolution tables) → open Manager (`/manager`, Server URL `http://localhost:8082` + API key from docker-compose.yml) → create instance `wau-guarderia` → Connect → scan QR with the platform phone. Optional data backup: `docker exec wau-postgres pg_dump -U postgres wau_db > backup-wau.sql` (same for evolution-postgres).
