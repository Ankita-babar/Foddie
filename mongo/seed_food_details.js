use("foodie");

db.food_details.deleteMany({});

db.food_details.insertMany([
  { _id: 1, description: "Paneer cubes in a creamy tomato and butter gravy", ingredients: ["paneer", "tomato", "butter", "cream", "kasuri methi"], spiceLevel: "medium", vegetarian: true, calories: 420, tags: ["bestseller", "north indian"] },
  { _id: 2, description: "Soft tandoor-baked bread brushed with butter", ingredients: ["maida", "yogurt", "butter"], vegetarian: true, calories: 260, allergens: ["gluten", "dairy"] },
  { _id: 3, description: "Yellow lentils tempered with garlic, cumin and ghee", ingredients: ["toor dal", "garlic", "cumin", "ghee"], spiceLevel: "mild", vegetarian: true, calories: 310 },
  { _id: 5, description: "Classic cheese pizza with basil and tomato sauce", ingredients: ["pizza base", "mozzarella", "tomato sauce", "basil"], vegetarian: true, calories: 640, tags: ["kids favourite"] },
  { _id: 10, description: "Crispy chicken patty with lettuce and mayo", ingredients: ["chicken", "bun", "lettuce", "mayonnaise"], vegetarian: false, calories: 540, spiceLevel: "mild" },
  { _id: 13, description: "Crisp rice crepe filled with spiced potato", ingredients: ["rice", "urad dal", "potato", "onion"], vegetarian: true, calories: 380, tags: ["south indian", "breakfast"], reviews: [{ user: "Rohan", stars: 5, text: "Crisp and hot" }, { user: "Sneha", stars: 4, text: "Great chutney" }] },
  { _id: 17, description: "Slow-cooked basmati rice layered with marinated chicken", ingredients: ["basmati rice", "chicken", "saffron", "fried onion"], vegetarian: false, calories: 720, spiceLevel: "hot", tags: ["bestseller"] },
  { _id: 20, description: "Milk dumplings soaked in cardamom sugar syrup", ingredients: ["khoya", "sugar", "cardamom"], vegetarian: true, calories: 300, allergens: ["dairy"] },
  { _id: 38, description: "Mashed vegetable curry served with buttered pav", ingredients: ["potato", "tomato", "capsicum", "butter", "pav"], vegetarian: true, calories: 450, spiceLevel: "medium", tags: ["street food", "bestseller"] }
]);

printjson(db.food_details.countDocuments());
