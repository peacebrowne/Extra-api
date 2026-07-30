package com.example.extra.Services.Impl;

import com.example.extra.Exceptions.Custom.BadRequest;
import com.example.extra.Exceptions.Custom.InternalServerError;
import com.example.extra.Mappers.ImageMapper;
import com.example.extra.Services.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ImageServiceImpl implements ImageService {

    private final ImageMapper imageMapper;

    /**
     * Stores multiple images for a specific task in the database.
     * Each image is validated for allowed file extensions before insertion.
     * If the image list is null or empty, the method returns without performing any operation.
     *
     * @param taskId the unique identifier of the task to associate images with
     * @param images the list of image URLs or paths to be stored
     * @throws BadRequest if any image fails validation (e.g., invalid file extension)
     * @throws InternalServerError if an unexpected error occurs during image storage
     */
    @Override
    public void storeImages(String taskId, List<String> images) {
        try {
            if (images == null || images.isEmpty()) {
                return;
            }

            images.forEach(image -> {
                // Validate image extension before inserting into DB
                validateImages(image);
                imageMapper.insertTaskImage(taskId, image);
            });
        }catch (BadRequest e){
            log.error("Image validation error: {}", e.getMessage());
            throw e;
        }
        catch (Exception e){
            log.error("Unable to store images: {}", e.getMessage());
            throw new InternalServerError("Unexpected error occurred while storing images", e);
        }
    }

    @Override
    public void updateImages(String taskId, List<String> images) {
        deleteImages(taskId);
        storeImages(taskId, images);
    }

    @Override
    public void deleteImages(String taskId) {
        try {
            imageMapper.deleteTaskImage(taskId);
        } catch (Exception e) {
            throw new InternalServerError("Unexpected error occurred while deleting images", e);
        }
    }

    @Override
    public String getImageByIdentifier(String identifier) {
        try {
            return imageMapper.getUserImage(identifier);
        } catch (Exception e) {
            throw new InternalServerError("Unexpected error occurred while getting image by identifier", e);
        }
    }

    @Override
    public List<String> getImages(String taskId) {
        return imageMapper.getTaskImages(taskId);
    }

    private void validateImages (String image) {
        String extension = image.split("\\.")[3];
        if (!extension.equals("jpg") && !extension.equals("jpeg") && !extension.equals("png")) {
            throw new BadRequest("Invalid image extension");
        }
    }

}
