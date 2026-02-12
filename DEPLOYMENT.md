# OrderFlow Deployment Guide

## Local Development (H2 Database)
```bash
# Run with H2 in-memory database
mvn spring-boot:run

# Or with profile
mvn spring-boot:run -Dspring-boot.run.profiles=test
```

Access at: http://localhost:8080

## Production Deployment (Docker)

### Prerequisites
- Docker & Docker Compose installed
- MySQL 8.0
- Redis 7

### Step 1: Build the Application
```bash
mvn clean package -DskipTests
```

### Step 2: Configure Environment Variables

Create `.env` file:
```bash
cp .env.example .env
# Edit .env with your production values
```

### Step 3: Start with Docker Compose
```bash
# Start all services
docker-compose -f docker-compose.prod.yml up -d

# View logs
docker-compose -f docker-compose.prod.yml logs -f app

# Stop all services
docker-compose -f docker-compose.prod.yml down
```

### Step 4: Verify Deployment
```bash
# Check health
curl http://localhost:8080/actuator/health

# Register a user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "email": "admin@orderflow.com",
    "password": "Admin@123",
    "firstName": "Admin",
    "lastName": "User",
    "role": "ADMIN"
  }'

# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Admin@123"
  }'

# Use the token from login response
export TOKEN="your-jwt-token-here"

# Access protected endpoint
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/customers
```

## Database Migration

Liquibase is configured for production. To run migrations:
```bash
mvn liquibase:update
```

## Monitoring

- **Health Check**: http://localhost:8080/actuator/health
- **Metrics**: http://localhost:8080/actuator/metrics
- **Prometheus**: http://localhost:8080/actuator/prometheus

## Scaling

To scale the application:
```bash
docker-compose -f docker-compose.prod.yml up -d --scale app=3
```

## Troubleshooting

### Database Connection Issues
```bash
# Check MySQL is running
docker-compose -f docker-compose.prod.yml ps mysql

# View MySQL logs
docker-compose -f docker-compose.prod.yml logs mysql
```

### Application Not Starting
```bash
# View application logs
docker-compose -f docker-compose.prod.yml logs app

# Check if MySQL is ready
docker exec orderflow-mysql-prod mysqladmin ping -h localhost
```

## Production Checklist

- [ ] Change default passwords in `.env`
- [ ] Set strong JWT secret (minimum 256 bits)
- [ ] Configure SSL/TLS certificates
- [ ] Set up database backups
- [ ] Configure log rotation
- [ ] Set up monitoring and alerting
- [ ] Review and update security settings
- [ ] Test disaster recovery procedures
