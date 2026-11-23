# Foodie - Online Food Delivery Management System

DBMS project. Focus: database design, SQL queries and backend.

**Stack:** MySQL 8 (relational data) | MongoDB (food images and dish details) | Java 21 | Spring Boot 3 (Spring JDBC + Spring Data MongoDB) | plain HTML/CSS/JS frontend

## Project structure

```
foodie/
├── pom.xml
├── sql/
│   ├── schema.sql        tables, constraints, indexes (re-runnable)
│   ├── data.sql          sample data: 20 customers, 15 restaurants, 50 foods, 20 orders, 39 items, 20 payments
│   └── queries.sql       the 8 reports + EXPLAIN for the indexes
├── docs/
│   ├── ER_AND_DESIGN.md  ER diagram, schema, normalization, index justification
│   └── VIVA_QA.md        expected viva questions with answers
└── src/main
    ├── java/com/foodie
    │   ├── controller/   REST endpoints
    │   ├── service/      OrderService (transactions, business rules)
    │   ├── repository/   all SQL (JdbcTemplate)
    │   ├── model/        records for tables and requests
    │   └── config/       exception handling
    └── resources
        ├── application.properties
        └── static/       index.html + shop.js + shop.css (customer page), admin.html + app.js + style.css (admin)
```

## How to run

1. Install JDK 21, Maven and MySQL 8.
2. Create the database and load data:
   ```
   mysql -u root -p < sql/schema.sql
   mysql -u root -p < sql/data.sql
   ```
3. Start MongoDB and load the sample dish details (see `docs/MONGODB.md`):
   ```
   brew services start mongodb-community
   mongosh < mongo/seed_food_details.js
   ```
4. Set your MySQL password (or edit `application.properties`):
   ```
   export DB_USER=root
   export DB_PASSWORD=your_password
   ```
5. Start the app:
   ```
   mvn spring-boot:run
   ```
6. Open http://localhost:8080 (customer page) or http://localhost:8080/admin.html (admin dashboard)

To run the SQL reports directly: `mysql -u root -p < sql/queries.sql`

## Requirement checklist

| Requirement | Where |
|---|---|
| At least 3 tables with relationships | 6 tables, 5 foreign keys (`sql/schema.sql`) |
| Full CRUD | customers, restaurants, foods, orders, order-items, payments |
| At least 5 meaningful queries | 8 queries (`sql/queries.sql`, `ReportRepository`, `/api/reports/*`) |
| At least 2 indexes with justification | `idx_orders_customer`, `idx_orders_date`, `idx_food_category` (`docs/ER_AND_DESIGN.md`) |
| 15-20 sample records per main table | customers 20, restaurants 15, orders 20, payments 20 (food_items 50, order_items 39) |
| Backend connected to database | Spring JDBC with MySQL connector |

If your faculty wants strictly 20 or fewer records per table, delete rows from the `food_items` block in `data.sql`
(keep at least 5 restaurants with 4 foods so Query 8 returns rows).

## Data flow

```
Browser (index.html + app.js)
   -> REST Controller        validates input (@Valid), maps URL to method
   -> Service (orders)       business rules + @Transactional
   -> Repository             parameterised SQL through JdbcTemplate
   -> MySQL (foodie)         enforces PK, FK, UNIQUE, CHECK, ENUM
```

Placing an order (`POST /api/orders`), all in one transaction:
1. check the customer exists
2. insert the order row
3. for each item: read the current price from `food_items`, check availability, insert into `order_items` with that price
4. recalculate `orders.total_amount` using `SUM(quantity * unit_price)`
5. insert the `payments` row (CARD/UPI = PAID, COD = PENDING)

If any step fails, everything is rolled back.

## REST API

| Resource | Endpoints |
|---|---|
| Customers | `GET/POST /api/customers`, `GET/PUT/DELETE /api/customers/{id}`, `GET /api/customers/{id}/orders` |
| Restaurants | `GET/POST /api/restaurants`, `GET/PUT/DELETE /api/restaurants/{id}`, `GET /api/restaurants/{id}/foods` |
| Food items | `GET/POST /api/foods`, `GET/PUT/DELETE /api/foods/{id}` |
| Orders | `GET/POST /api/orders`, `GET/DELETE /api/orders/{id}`, `PUT /api/orders/{id}/status` |
| Order items | `GET/POST /api/order-items` (`?orderId=`), `GET/PUT/DELETE /api/order-items/{id}` |
| Payments | `GET/POST /api/payments`, `GET/PUT/DELETE /api/payments/{id}` |
| Reports | `/api/reports/available-foods`, `orders-with-customers`, `orders-per-customer`, `top-selling-foods`, `restaurant-revenue`, `above-average-customers`, `monthly-stats`, `restaurants-with-many-dishes` |

## Quick tests with curl

```
curl http://localhost:8080/api/foods

curl -X POST http://localhost:8080/api/customers -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@example.com","phone":"9876543210","address":"Pune"}'

curl -X POST http://localhost:8080/api/orders -H "Content-Type: application/json" \
  -d '{"customerId":1,"paymentMethod":"UPI","items":[{"foodId":1,"quantity":2},{"foodId":2,"quantity":3}]}'

curl -X PUT http://localhost:8080/api/orders/1/status -H "Content-Type: application/json" -d '{"status":"DELIVERED"}'

curl http://localhost:8080/api/reports/restaurant-revenue
```

Error responses are JSON: 400 for validation, 404 when a record is missing, 409 when a database constraint blocks the
operation (for example deleting a customer who has orders).
