# Foodie - ER Diagram and Database Design

## ER diagram

```mermaid
erDiagram
    CUSTOMERS ||--o{ ORDERS : places
    ORDERS ||--|{ ORDER_ITEMS : contains
    FOOD_ITEMS ||--o{ ORDER_ITEMS : "appears in"
    RESTAURANTS ||--o{ FOOD_ITEMS : offers
    ORDERS ||--o| PAYMENTS : "paid by"

    CUSTOMERS {
        int customer_id PK
        varchar name
        varchar email UK
        varchar phone
        varchar address
    }
    RESTAURANTS {
        int restaurant_id PK
        varchar name
        varchar address
        varchar phone
        decimal rating
    }
    FOOD_ITEMS {
        int food_id PK
        int restaurant_id FK
        varchar food_name
        varchar category
        decimal price
        boolean availability
    }
    ORDERS {
        int order_id PK
        int customer_id FK
        datetime order_date
        enum status
        decimal total_amount
    }
    ORDER_ITEMS {
        int order_item_id PK
        int order_id FK
        int food_id FK
        int quantity
        decimal unit_price
    }
    PAYMENTS {
        int payment_id PK
        int order_id FK_UK
        enum payment_method
        enum payment_status
        decimal amount
    }
```

Paste the Mermaid block into https://mermaid.live to export it as an image for your report.

## Relationships

| Relationship | Type | Foreign key | On delete |
|---|---|---|---|
| Customer places Orders | 1 : N | orders.customer_id | RESTRICT (customer with orders cannot be deleted) |
| Restaurant offers Food items | 1 : N | food_items.restaurant_id | CASCADE |
| Order contains Order items | 1 : N | order_items.order_id | CASCADE |
| Food item appears in Order items | 1 : N | order_items.food_id | RESTRICT (sold food cannot be deleted; mark unavailable instead) |
| Order has Payment | 1 : 0..1 | payments.order_id (UNIQUE) | CASCADE |

`order_items` is the junction table that resolves the many-to-many relationship between orders and food items.

## Integrity rules in the schema

- `customers.email` UNIQUE
- `rating` between 0 and 5, `price` > 0, `quantity` > 0 (CHECK)
- `orders.status`, `payments.payment_method`, `payments.payment_status` are ENUMs
- `UNIQUE (order_id, food_id)` in order_items: the same dish appears once per order; adding it again increases the quantity
- `unit_price` is copied from `food_items.price` at order time, so later menu price changes do not alter old orders

## Normalization

- 1NF: all columns are atomic, every table has a primary key.
- 2NF: every non-key column depends on the whole key. Item details live in `food_items`, not in `order_items`.
- 3NF: no transitive dependencies. Customer details are in `customers`, restaurant details in `restaurants`.
- `orders.total_amount` is a deliberate derived value kept for fast reporting. It is recalculated from `order_items`
  inside the same transaction whenever items change.

## Indexes and justification

| Index | Column | Why |
|---|---|---|
| `idx_orders_customer` | orders(customer_id) | Speeds up "orders of a customer" and the customers-orders joins (Q2, Q3, Q6) |
| `idx_orders_date` | orders(order_date) | Speeds up date-range filtering and monthly reports (Q7) |
| `idx_food_category` | food_items(category) | Speeds up browsing the menu by category |

Verify with `EXPLAIN` (included at the end of `sql/queries.sql`). Without the index the plan shows `type: ALL`
(full table scan); with it the plan shows `type: ref` or `range` and the index name in `key`.

Trade-off: every index uses extra storage and slows INSERT/UPDATE slightly, so only columns used in WHERE/JOIN clauses
are indexed. MySQL InnoDB already creates indexes for foreign keys, primary keys and UNIQUE columns.

## Database choice: why MySQL

The data is structured and strongly related (customers - orders - items - food). A relational database gives foreign
keys, joins, constraints and ACID transactions, which are needed when an order, its items and its payment must be saved
together or not at all. A document database like MongoDB would duplicate data and make the revenue and join queries harder.
