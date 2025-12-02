package com.dsevSport.DSEV_Sport.commerce.service;

import com.google.cloud.storage.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GcsService {

    private final Storage storage;

    @Value("${gcs.bucket}")
    private String bucket;

    public String upload(MultipartFile file, String folder) {

        validate(file);

        try {
            String filename = folder + "/" + UUID.randomUUID() + "_" +
                    Objects.requireNonNull(file.getOriginalFilename()).replace(" ", "_");

            BlobId blobId = BlobId.of(bucket, filename);

            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();

            storage.create(blobInfo, file.getBytes());

            System.out.println("GOOGLE_APPLICATION_CREDENTIALS = " +
                    System.getenv("GOOGLE_APPLICATION_CREDENTIALS"));

            return "https://storage.googleapis.com/" + bucket + "/" + filename;

        } catch (IOException e) {
            throw new RuntimeException("Upload to GCS failed", e);
        }

    }

    /**
     * Xóa ảnh khỏi Google Cloud Storage (khi delete ảnh)
     */
    public void deleteByUrl(String imageUrl) {

        String prefix = "https://storage.googleapis.com/" + bucket + "/";
        if (!imageUrl.startsWith(prefix)) {
            throw new RuntimeException("Invalid GCS URL");
        }

        String blobName = imageUrl.replace(prefix, "");

        boolean deleted = storage.delete(bucket, blobName);
        if (!deleted) {
            throw new RuntimeException("Delete failed: " + blobName);
        }
    }

    /**
     * Validate file upload
     */
    private void validate(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image")) {
            throw new RuntimeException("Only image allowed");
        }

        long max = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > max) {
            throw new RuntimeException("Max file size is 5MB");
        }
    }
}
