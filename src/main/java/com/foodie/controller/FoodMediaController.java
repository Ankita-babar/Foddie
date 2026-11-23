package com.foodie.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.foodie.service.FoodMediaService;

@RestController
public class FoodMediaController {

    private final FoodMediaService media;

    public FoodMediaController(FoodMediaService media) {
        this.media = media;
    }

    @PostMapping(value = "/api/foods/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> uploadImage(@PathVariable int id, @RequestParam("file") MultipartFile file)
            throws IOException {
        media.saveImage(id, file);
        return Map.of("foodId", id, "message", "Image stored in MongoDB");
    }

    @GetMapping("/api/foods/{id}/image")
    public ResponseEntity<GridFsResource> image(@PathVariable int id) {
        GridFsResource image = media.findImage(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(image.getContentType()))
                .body(image);
    }

    @DeleteMapping("/api/foods/{id}/image")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteImage(@PathVariable int id) {
        media.deleteImage(id);
    }

    @GetMapping("/api/foods/{id}/details")
    public Document details(@PathVariable int id) {
        return media.findDetails(id);
    }

    @PutMapping("/api/foods/{id}/details")
    public Document saveDetails(@PathVariable int id, @RequestBody Map<String, Object> body) {
        return media.saveDetails(id, body);
    }

    @DeleteMapping("/api/foods/{id}/details")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDetails(@PathVariable int id) {
        media.deleteDetails(id);
    }

    @GetMapping("/api/food-details")
    public List<Document> allDetails() {
        return media.findAllDetails();
    }
}
