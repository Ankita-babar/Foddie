package com.foodie.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.foodie.config.BadRequestException;
import com.foodie.config.NotFoundException;
import com.foodie.repository.FoodItemRepository;
import com.mongodb.client.gridfs.model.GridFSFile;

@Service
public class FoodMediaService {

    private static final Logger log = LoggerFactory.getLogger(FoodMediaService.class);
    private static final String DETAILS = "food_details";

    private final GridFsTemplate gridFs;
    private final MongoTemplate mongo;
    private final FoodItemRepository foods;

    public FoodMediaService(GridFsTemplate gridFs, MongoTemplate mongo, FoodItemRepository foods) {
        this.gridFs = gridFs;
        this.mongo = mongo;
        this.foods = foods;
    }

    public void saveImage(int foodId, MultipartFile file) throws IOException {
        requireFood(foodId);
        String type = file.getContentType();
        if (file.isEmpty() || type == null || !type.startsWith("image/")) {
            throw new BadRequestException("Upload a non-empty image file (jpg, png, webp)");
        }
        gridFs.delete(imageQuery(foodId));
        try (InputStream in = file.getInputStream()) {
            gridFs.store(in, "food-" + foodId, type, new Document("foodId", foodId));
        }
    }

    public GridFsResource findImage(int foodId) {
        GridFSFile file = gridFs.findOne(imageQuery(foodId));
        if (file == null) {
            throw new NotFoundException("No image stored for food item " + foodId);
        }
        return gridFs.getResource(file);
    }

    public void deleteImage(int foodId) {
        gridFs.delete(imageQuery(foodId));
    }

    public Document findDetails(int foodId) {
        requireFood(foodId);
        Document details = mongo.findById(foodId, Document.class, DETAILS);
        if (details == null) {
            throw new NotFoundException("No details stored for food item " + foodId);
        }
        return details;
    }

    public List<Document> findAllDetails() {
        return mongo.findAll(Document.class, DETAILS);
    }

    public Document saveDetails(int foodId, Map<String, Object> body) {
        requireFood(foodId);
        Document details = new Document(body);
        details.put("_id", foodId);
        mongo.save(details, DETAILS);
        return details;
    }

    public void deleteDetails(int foodId) {
        mongo.remove(Query.query(Criteria.where("_id").is(foodId)), DETAILS);
    }

    public void removeAll(int foodId) {
        try {
            deleteImage(foodId);
            deleteDetails(foodId);
        } catch (RuntimeException e) {
            log.warn("Could not clean MongoDB data of food item {}: {}", foodId, e.getMessage());
        }
    }

    private Query imageQuery(int foodId) {
        return Query.query(Criteria.where("metadata.foodId").is(foodId));
    }

    private void requireFood(int foodId) {
        if (foods.findById(foodId).isEmpty()) {
            throw new NotFoundException("Food item " + foodId + " not found");
        }
    }
}
