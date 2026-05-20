# Warehouse Ops Suite Runtime

Docker Compose 기반 로컬 데모 런타임입니다.

```bash
docker compose up --build
```

서비스 URL:

| Service | URL |
| --- | --- |
| Admin Web | http://localhost:4173 |
| API Health | http://localhost:8080/actuator/health |
| PDF Renderer | http://localhost:4050/health |
| Print Agent | http://localhost:4020/health |
| DPS Agent | http://localhost:4030/health |
