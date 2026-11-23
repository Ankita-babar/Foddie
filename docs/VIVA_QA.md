# Foodie - Viva Questions and Answers

**1. Why did you choose MySQL?**
The data is relational and needs foreign keys, joins and transactions. MySQL supports all of these, and InnoDB gives ACID guarantees.

**2. Explain your ER diagram.**
Customers place orders (1:N). Orders contain order items (1:N). Food items appear in order items (1:N), so order_items is the
junction table for the many-to-many between orders and food. Restaurants offer food items (1:N). Each order has at most one payment (1:1, UNIQUE foreign key).

**3. Why is unit_price stored in order_items when food_items already has price?**
Menu prices change. Storing the price at order time keeps old orders and revenue reports correct.

**4. Why is total_amount stored in orders if it can be calculated?**
It is a deliberate denormalization for fast reporting. It is recalculated with SUM(quantity * unit_price) in the same
transaction whenever items change, so it never goes out of sync.

**5. What happens when you delete a customer who has orders?**
The foreign key uses ON DELETE RESTRICT, so MySQL rejects the delete. The API returns HTTP 409.

**6. What happens when you delete an order?**
ON DELETE CASCADE removes its order_items and its payment automatically.

**7. Explain the flow when an order is placed.**
Controller validates the JSON, OrderService starts a transaction, checks the customer, inserts the order, inserts each item with the
current price, recalculates the total, inserts the payment, commits. Any failure rolls everything back.

**8. What is a transaction and why is it used here?**
A group of operations that succeed or fail together (ACID). Without it, a failure could leave an order with no items or no payment.

**9. Which indexes did you create and why?**
idx_orders_customer (customer lookup and joins), idx_orders_date (date range and monthly reports), idx_food_category (menu by category).
Checked with EXPLAIN: the plan uses the index (`ref` / `range`) instead of a full scan.

**10. What is the drawback of indexes?**
Extra storage and slower inserts/updates, because the index must be maintained.

**11. Explain Query 3. Why LEFT JOIN?**
It counts orders per customer. LEFT JOIN keeps customers with zero orders (count 0); an INNER JOIN would drop them.

**12. Explain Query 6.**
The inner subquery computes the overall average order value. The middle query groups orders by customer and keeps customers whose
own average is higher (HAVING). The outer query fetches their names.

**13. Difference between WHERE and HAVING?**
WHERE filters rows before grouping; HAVING filters groups after aggregation (Query 8: HAVING COUNT(...) > 3).

**14. How do Queries 4 and 5 ignore cancelled orders?**
`WHERE o.status <> 'CANCELLED'`, because cancelled orders should not count as sales.

**15. What is normalization? Which normal form is your design in?**
Removing redundancy by splitting data into related tables. The design is in 3NF, with total_amount as the one deliberate derived column.

**16. How do you prevent SQL injection?**
All queries use JdbcTemplate with `?` placeholders (prepared statements). User input is never concatenated into SQL.

**17. How are validation errors handled?**
Bean Validation annotations on the model records, `@Valid` in controllers, and one `@RestControllerAdvice` that returns 400, 404 or 409 as JSON.

**18. What is the difference between PRIMARY KEY, UNIQUE and FOREIGN KEY?**
PRIMARY KEY identifies a row (unique, not null, one per table). UNIQUE prevents duplicates in a column (email, payments.order_id).
FOREIGN KEY links a column to a primary key in another table and enforces referential integrity.

**19. Why is payments.order_id UNIQUE?**
It limits each order to one payment record, which gives the 1:1 relationship.

**20. What happens to the payment when an order is cancelled?**
OrderService changes PAID to REFUNDED and PENDING to FAILED, in the same transaction as the status change.
