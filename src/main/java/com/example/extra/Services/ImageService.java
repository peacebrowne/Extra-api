package com.example.extra.Services;

import java.util.List;

public interface ImageService {
    public void storeImages (String taskId, List<String> images);
    public void updateImages (String taskId, List<String> images);
    public void deleteImages (String taskId);
    public List<String> getImages (String taskId);
    public String getImageByIdentifier(String identifier);
}
