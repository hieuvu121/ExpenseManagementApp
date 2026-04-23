# AWS Deployment Design — Expensphie

**Date:** 2026-04-21  
**Status:** Approved

## Overview

Deploy Expensphie as a 3-tier architecture on AWS:
- **Frontend:** S3 + CloudFront
- **Backend:** 2× EC2 instances (backend + email service) behind ALB
- **Infra:** 1× EC2 instance (Redis + Kafka)
- **Database:** RDS MySQL

---

## Architecture Diagram

```
Internet
   │
   ├── app.yourdomain.com ──► CloudFront ──► S3 (React build)
   │
   └── api.yourdomain.com ──► Route 53 ──► ALB (ACM SSL, HTTPS 443)
                                               │
                                    ┌──────────┴──────────┐
                                    ▼                     ▼
                              EC2 Backend-1         EC2 Backend-2
                           (Private Subnet AZ-a) (Private Subnet AZ-b)
                           ┌──────────────┐      ┌──────────────┐
                           │ Spring Boot  │      │ Spring Boot  │
                           │ EmailService │      │ EmailService │
                           └──────────────┘      └──────────────┘
                                    │                     │
                                    └──────────┬──────────┘
                                               │
                                    EC2 Infra (Private Subnet AZ-a)
                                   ┌─────────────────────┐
                                   │  Redis   :6379       │
                                   │  Kafka   :9092       │
                                   └─────────────────────┘
                                               │
                                     RDS MySQL (Private Subnet AZ-a)
```

---

## 1. Compute Resources

| Resource | Type | Purpose |
|---|---|---|
| ALB | Application Load Balancer | HTTPS termination, routes to 2 backends, sticky sessions for WebSocket |
| EC2 Backend-1 | t3.medium | Spring Boot + EmailService (Docker Compose) |
| EC2 Backend-2 | t3.medium | Spring Boot + EmailService (Docker Compose) |
| EC2 Infra | t3.medium | Redis + Kafka (Docker Compose) |
| RDS | t3.micro MySQL 8 | Primary database |

**Estimated cost:** ~$120-140/month (includes NAT Gateway ~$32/mo)

---

## 2. Networking (VPC)

**CIDR:** `10.0.0.0/16`

```
VPC (10.0.0.0/16)
├── Public Subnet AZ-a/b        ── ALB + NAT Gateway
├── Private Subnet AZ-a         ── EC2 Backend-1
├── Private Subnet AZ-b         ── EC2 Backend-2
├── Private Subnet AZ-a         ── EC2 Infra (Redis+Kafka)
└── Private Subnet AZ-a + AZ-b  ── RDS DB Subnet Group (AWS requires 2 AZs minimum; RDS runs in AZ-a)
```

**Security Groups:**

| Resource | Inbound Port | Source |
|---|---|---|
| ALB | 443 | 0.0.0.0/0 |
| EC2 Backend-1/2 | 8080, 8081 | ALB Security Group |
| EC2 Infra | 6379, 9092 | Backend Security Group |
| RDS | 3306 | Backend Security Group |

- NAT Gateway in public subnet for private EC2 outbound internet (Google Gemini API, Gmail SMTP)
- SSH access via AWS Systems Manager Session Manager (no public port 22 needed)
- ALB spans both AZ-a and AZ-b public subnets for high availability
- ALB target group: sticky sessions enabled for STOMP WebSocket support

---

## 3. Domain & SSL

**Domain:** Register via Route 53.

**DNS Records:**
```
app.yourdomain.com  → CloudFront distribution (Alias)
api.yourdomain.com  → ALB DNS name (Alias)
```

**ACM Certificates (free, auto-renewing):**

| Certificate | Region | Used By |
|---|---|---|
| `app.yourdomain.com` | `us-east-1` | CloudFront (must be us-east-1) |
| `api.yourdomain.com` | your EC2 region | ALB |

**CloudFront Configuration:**
- Origin: S3 bucket (static website hosting enabled)
- Redirect HTTP → HTTPS
- Cache: optimized (cache hashed assets, `no-cache` for `index.html`)
- Custom error responses: 403/404 → `/index.html` with 200 status (React Router SPA support)

---

## 4. Docker Compose Split

### `docker-compose.backend.yml` (EC2 Backend-1 & Backend-2)
```yaml
services:
  backend:
    image: expensphie-backend:latest
    ports: ["8080:8080"]
    environment:
      DB_URL: ${DB_URL}           # RDS endpoint
      DB_USERNAME: ${DB_USERNAME}
      DB_PASSWORD: ${DB_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      REDIS_HOST: ${REDIS_HOST}   # EC2 Infra private IP
      REDIS_PORT: 6379
      SPRING_KAFKA_BOOTSTRAP_SERVERS: ${KAFKA_HOST}:9092
      GOOGLE_GENAI_API_KEY: ${GOOGLE_GENAI_API_KEY}
      APP_CORS_ALLOWED_ORIGINS: https://app.yourdomain.com

  email_service:
    image: expensphie-email:latest
    ports: ["8081:8081"]
    environment:
      MAIL_USERNAME: ${MAIL_USERNAME}
      MAIL_PASSWORD: ${MAIL_PASSWORD}
      MAIL_FROM_EMAIL: ${MAIL_FROM_EMAIL}
      MAIL_HOST: ${MAIL_HOST}
      DB_URL: ${DB_URL}
      DB_USERNAME: ${DB_USERNAME}
      DB_PASSWORD: ${DB_PASSWORD}
      SPRING_KAFKA_BOOTSTRAP_SERVERS: ${KAFKA_HOST}:9092
```

### `docker-compose.infra.yml` (EC2 Infra)
```yaml
services:
  kafka:
    image: confluentinc/cp-kafka:7.9.6
    container_name: kafka
    ports: ["9092:9092"]
    environment:
      CLUSTER_ID: "MkU3OEVBNTcwNTJENDM2Qk"
      KAFKA_KRAFT_MODE: 'true'
      KAFKA_NODE_ID: 1
      KAFKA_PROCESS_ROLES: broker,controller
      KAFKA_LISTENERS: PLAINTEXT://:9092,CONTROLLER://:9093
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://${INFRA_PRIVATE_IP}:9092
      KAFKA_CONTROLLER_LISTENER_NAMES: CONTROLLER
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT
      KAFKA_CONTROLLER_QUORUM_VOTERS: 1@kafka:9093
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_LOG_DIRS: /tmp/kraft-combined-logs
    volumes:
      - kafka_kraft:/var/lib/kafka/data

  redis:
    image: redis:latest
    container_name: redis
    ports: ["6379:6379"]

volumes:
  kafka_kraft:
```

**Critical config change:** `KAFKA_ADVERTISED_LISTENERS` must use EC2 Infra's **private IP**, not the `kafka` hostname, so backend EC2s can reach it across the VPC.

---

## 5. CI/CD (GitHub Actions)

### Workflow 1: Frontend Deploy (`frontend.yml`)
Triggered on push to `main` when `frontend/**` changes.
```
1. npm run build (VITE_API_BASE_URL=https://api.yourdomain.com/app/v1)
2. aws s3 sync ./dist s3://${S3_BUCKET_NAME} --delete
3. aws cloudfront create-invalidation --distribution-id ${CLOUDFRONT_DISTRIBUTION_ID} --paths "/*"
```

### Workflow 2: Backend Deploy (`backend.yml`)
Triggered on push to `main` when `expensphie_backend/**` or `emailService/**` changes.
```
1. mvn clean package -DskipTests (backend + email service)
2. Upload JARs to EC2 Backend-1 via SSM
3. Restart services via docker-compose
4. Health check (GET /app/v1/actuator/health)
5. Repeat for EC2 Backend-2 (rolling update)
```

### Workflow 3: Infra Deploy (`infra.yml`)
Triggered on push to `main` when `docker-compose.infra.yml` changes.
```
1. SSM to EC2 Infra
2. docker-compose -f docker-compose.infra.yml pull
3. docker-compose -f docker-compose.infra.yml up -d
```

### GitHub Secrets Required
```
AWS_ACCESS_KEY_ID
AWS_SECRET_ACCESS_KEY
AWS_REGION
S3_BUCKET_NAME
CLOUDFRONT_DISTRIBUTION_ID
EC2_BACKEND_1_INSTANCE_ID   (SSM instance ID)
EC2_BACKEND_2_INSTANCE_ID
EC2_INFRA_INSTANCE_ID
```

---

## 6. Environment Variables Summary

| Variable | Backend EC2 | Infra EC2 |
|---|---|---|
| `DB_URL` | RDS endpoint | — |
| `REDIS_HOST` | EC2 Infra private IP | — |
| `SPRING_KAFKA_BOOTSTRAP_SERVERS` | EC2 Infra private IP:9092 | — |
| `INFRA_PRIVATE_IP` | — | Self private IP (for Kafka advertised listener) |
| `APP_CORS_ALLOWED_ORIGINS` | `https://app.yourdomain.com` | — |
| `VITE_API_BASE_URL` | — (build time) | — |

---

## 7. Provisioning Order

1. Register domain in Route 53
2. Create VPC, subnets, NAT Gateway, Internet Gateway, route tables
3. Create Security Groups
4. Launch EC2 Infra → start Redis + Kafka
5. Provision RDS MySQL
6. Launch EC2 Backend-1 and Backend-2 → configure `.env` with Infra private IP and RDS endpoint
7. Create ALB + target group (sticky sessions on) → register backend EC2s
8. Request ACM certificates → attach to ALB and CloudFront
9. Create S3 bucket → enable static website hosting
10. Create CloudFront distribution → point to S3
11. Create Route 53 Alias records for `app.*` and `api.*`
12. Configure GitHub Actions secrets
13. Push to `main` → validate full deployment
