package com.dsevSport.DSEV_Sport.commerce.service;

import com.dsevSport.DSEV_Sport.commerce.dto.response.ProductImageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface ProductImageService {
    List<ProductImageResponse> uploadImages(UUID productId, List<MultipartFile> files);

    List<ProductImageResponse> getByProduct(UUID productId);

    void deleteImages(UUID imageId);
}
