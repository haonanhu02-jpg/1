# Iyque Docker Deployment

## Files

- `Dockerfile`: backend image, Java 17 plus Spring Boot jar.
- `frontEnd/pc/Dockerfile`: PC web image, Vue build plus Nginx.
- `docker-compose.yml`: web, backend, MySQL, Redis, volumes, and network.
- `.env.example`: runtime configuration template. Copy it to `.env` and edit secrets before starting.
- `deploy/nginx/default.conf`: Nginx static web and `/api` reverse proxy.
- `deploy/mysql/init`: SQL files imported by MySQL only when `mysql_data` is empty.

## Start

```bash
cp .env.example .env
docker compose up -d --build
```

Open:

```text
http://localhost:8080/tools/
```

## Notes

- `mysql_data` stores real database data. Do not delete it in production unless you intend to reset the database.
- `backend_uploads` stores uploaded files and generated assets.
- MySQL init scripts run only on first startup with an empty `mysql_data` volume.
- Later schema upgrades should be executed deliberately with `docker compose exec iyque-mysql mysql ...` or a migration tool.
- Public WeCom callbacks still need a real HTTPS domain that forwards to `/iycallback/handle`.
