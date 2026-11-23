# MongoDB (NoSQL) in Foodie

## Why a second database
MySQL stores the structured, related data (customers, orders, payments) where integrity and transactions matter.
Some data does not fit a fixed table:
- **Food images** are binary files up to several MB.
- **Dish details** differ from dish to dish: a biryani has a spice level, a dessert has allergens, some dishes have reviews, most have none.

MongoDB suits this data: GridFS stores files, and documents have a flexible schema. Using each database for what it does best is called **polyglot persistence**.

## What is stored where
| Data | Database | Why |
|---|---|---|
| customers, restaurants, food_items, orders, order_items, payments | MySQL | related data, constraints, joins, transactions |
| food images | MongoDB GridFS (`fs.files`, `fs.chunks`) | large binary data |
| dish details (description, ingredients, tags, allergens, reviews) | MongoDB collection `food_details` | flexible, nested data |

Image metadata holds `foodId`, and `food_details._id` is the `food_id`, so a MongoDB record always points to a row in `food_items`.
There is no foreign key across two databases, so the backend checks that the food item exists in MySQL before saving anything in MongoDB,
and deleting a food item also removes its image and details.

## Setup on Mac
```
brew tap mongodb/brew
brew install mongodb-community
brew services start mongodb-community
brew install mongosh
mongosh --eval "db.runCommand({ ping: 1 })"
```

## Load sample details
```
mongosh < mongo/seed_food_details.js
```

## Upload images
Rename pictures by food id (`1.jpg`, `2.png`, `13.jpg` ...) in a folder called `images`, then run:
```
./scripts/upload_images.sh images
```
Or use the Image button next to a food item in the admin page (http://localhost:8080/admin.html, Food Items tab).

## API
| Method | URL | Purpose |
|---|---|---|
| POST | /api/foods/{id}/image | upload image (form field `file`) |
| GET | /api/foods/{id}/image | get the image |
| DELETE | /api/foods/{id}/image | delete the image |
| GET / PUT / DELETE | /api/foods/{id}/details | read, save or delete the dish document |
| GET | /api/food-details | all dish documents |

Example:
```
curl -X POST -F "file=@images/1.jpg" http://localhost:8080/api/foods/1/image
curl -X PUT http://localhost:8080/api/foods/2/details -H "Content-Type: application/json" \
  -d '{"description":"Garlic naan","tags":["bread"],"allergens":["gluten"]}'
```

## Check in mongosh
```
mongosh
use foodie
show collections
db.food_details.find().pretty()
db.fs.files.find({}, { filename: 1, length: 1, contentType: 1, metadata: 1 }).pretty()
db.food_details.find({ vegetarian: true, calories: { $lt: 400 } })
db.food_details.find({ tags: "bestseller" }, { description: 1 })
db.food_details.aggregate([{ $group: { _id: "$vegetarian", total: { $sum: 1 }, avgCalories: { $avg: "$calories" } } }])
```

## Viva answers
- **Why not store images in MySQL as BLOB?** It bloats the relational tables and backups and slows queries. GridFS is built for files and splits them into chunks.
- **Why GridFS?** A normal MongoDB document is limited to 16 MB. GridFS splits files into 255 KB chunks stored in `fs.chunks`, with details in `fs.files`.
- **How are the two databases linked?** By `food_id`. There is no cross-database foreign key, so the application checks MySQL first.
- **What happens if MongoDB is down?** Orders, customers and reports keep working because they use only MySQL. Image and detail requests return 503, and the customer page simply shows no pictures.
- **SQL vs NoSQL?** SQL has a fixed schema, joins and ACID transactions. NoSQL such as MongoDB has a flexible schema and scales out easily, but weaker relationship support.
