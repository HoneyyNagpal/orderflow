# OrderFlow API Testing Guide

## Authentication Flow

### 1. Register a New User
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john@example.com",
    "password": "Password@123",
    "firstName": "John",
    "lastName": "Doe",
    "role": "CUSTOMER"
  }'
```

### 2. Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "password": "Password@123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer",
  "username": "johndoe"
}
```

Save this token for subsequent requests!

### 3. Use Token in Requests
```bash
# Set token as variable
export TOKEN="your-jwt-token-here"

# Make authenticated request
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/api/v1/customers
```

## Complete Workflow Example

### Create Category → Product → Customer → Order → Invoice → Payment
```bash
# 1. Login and get token
TOKEN=$(curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"johndoe","password":"Password@123"}' \
  | jq -r '.token')

# 2. Create Category
curl -X POST http://localhost:8080/api/v1/categories \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "code": "ELECTRONICS",
    "name": "Electronics",
    "description": "Electronic devices and accessories"
  }'

# 3. Create Product
PRODUCT=$(curl -X POST http://localhost:8080/api/v1/products \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "LAPTOP-001",
    "name": "Dell XPS 15",
    "description": "Premium laptop",
    "price": 1499.99,
    "quantityInStock": 50,
    "active": true
  }')

PRODUCT_ID=$(echo $PRODUCT | jq -r '.id')

# 4. Create Customer
CUSTOMER=$(curl -X POST http://localhost:8080/api/v1/customers \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Jane",
    "lastName": "Smith",
    "email": "jane@example.com",
    "phoneNumber": "9876543210",
    "active": true
  }')

CUSTOMER_ID=$(echo $CUSTOMER | jq -r '.id')

# 5. Create Order
ORDER=$(curl -X POST http://localhost:8080/api/v1/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"customer\": {\"id\": $CUSTOMER_ID},
    \"items\": [{
      \"product\": {\"id\": $PRODUCT_ID},
      \"quantity\": 2
    }]
  }")

ORDER_ID=$(echo $ORDER | jq -r '.id')

# 6. Generate Invoice
INVOICE=$(curl -X POST http://localhost:8080/api/v1/invoices/generate/order/$ORDER_ID \
  -H "Authorization: Bearer $TOKEN")

INVOICE_ID=$(echo $INVOICE | jq -r '.id')

# 7. Process Payment
curl -X POST http://localhost:8080/api/v1/payments/process/invoice/$INVOICE_ID \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 2999.98,
    "method": "CREDIT_CARD"
  }'
```

## Postman Collection

Import `OrderFlow-API.postman_collection.json` for complete API testing.
