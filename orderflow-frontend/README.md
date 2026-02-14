# OrderFlow

A full-stack order management system I built to learn Spring Boot and React together. It handles the usual business stuff — customers, products, orders, and payments - with a clean UI and proper authentication.

**Live demo:** https://orderflow-frontend-mu.vercel.app  
**Backend API:** https://orderflow-backend-ezt5.onrender.com

> Note: The backend is on Render's free tier so the first request after inactivity takes about 30 seconds to wake up.

---

## What it does

- Register and login with JWT authentication
- Manage customers (add, edit, delete, segments like VIP/Regular)
- Manage products (add, edit, delete, stock tracking with low-stock warnings)
- Create orders with multiple items, track status, cancel orders
- Record payments with different methods (UPI, cards, net banking, etc.)
- Dashboard that shows live stats — total customers, products, orders, revenue

---

## Tech stack

**Backend**
- Java 17 + Spring Boot 3.2
- Spring Security with JWT
- Spring Data JPA + Hibernate
- PostgreSQL (Neon) in production, H2 locally

**Frontend**
- React 18
- Material-UI v5
- Axios for API calls
- React Router v6

**Deployed on**
- Backend → Render
- Frontend → Vercel
- Database → Neon (PostgreSQL)

---

## Running locally

You'll need Java 17, Node.js 18, and Maven installed.

**Backend**
```bash
git clone https://github.com/HoneyyNagpal/orderflow.git
cd orderflow
mvn spring-boot:run
```
Runs on http://localhost:8080  
H2 console at http://localhost:8080/h2-console

**Frontend**
```bash
cd orderflow-frontend
npm install
npm start
```
Runs on http://localhost:3000

---

## Project structure

```
orderflow/
├── src/main/java/com/orderflow/
│   ├── config/          # Security, CORS, Jackson
│   ├── controller/      # REST controllers
│   ├── model/
│   │   ├── entity/      # JPA entities
│   │   ├── dto/         # Response DTOs
│   │   └── enums/       # Status enums
│   ├── repository/      # JPA repositories
│   ├── service/         # Business logic
│   └── exception/       # Error handling
│
orderflow-frontend/
├── src/
│   ├── components/
│   │   ├── Auth/        # Login, Register
│   │   ├── Dashboard/
│   │   ├── Customers/
│   │   ├── Products/
│   │   ├── Orders/
│   │   ├── Payments/
│   │   └── Layout/
│   ├── contexts/        # Auth context
│   └── services/        # API calls
```

---

## API

All endpoints except auth require a Bearer token in the Authorization header.

```
POST   /api/v1/auth/register
POST   /api/v1/auth/login

GET    /api/v1/customers
POST   /api/v1/customers
PUT    /api/v1/customers/{id}
DELETE /api/v1/customers/{id}

GET    /api/v1/products
POST   /api/v1/products
PUT    /api/v1/products/{id}
DELETE /api/v1/products/{id}

GET    /api/v1/orders
POST   /api/v1/orders
PATCH  /api/v1/orders/{id}/status
POST   /api/v1/orders/{id}/cancel

POST   /api/v1/payments
```

---

## Things I want to add later

- Invoice PDF generation
- Email notifications when order status changes
- Charts on the dashboard
- Search and filters on the tables
- Switch to MySQL for production

---

## Author

Honey Nagpal  
GitHub: https://github.com/HoneyyNagpal