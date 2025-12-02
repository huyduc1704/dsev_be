package com.dsevSport.DSEV_Sport.commerce.service;

import com.dsevSport.DSEV_Sport.commerce.dto.response.ProductImageResponse;
import com.dsevSport.DSEV_Sport.commerce.mapper.ProductImageMapper;
import com.dsevSport.DSEV_Sport.commerce.model.Product;
import com.dsevSport.DSEV_Sport.commerce.model.ProductImage;
import com.dsevSport.DSEV_Sport.commerce.repository.ProductImageRepository;
import com.dsevSport.DSEV_Sport.commerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductImageServiceImpl implements ProductImageService {
    private final ProductRepository productRepository;
    private final ProductImageRepository imageRepository;
    private final ProductImageMapper mapper;
    private final GcsService gcsService;
    @Override
    public List<ProductImageResponse> uploadImages(UUID productId, List<MultipartFile> files) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        List<ProductImageResponse> responses = new ArrayList<>();

        System.out.println("FILES NULL? " + (files == null));
        System.out.println("FILES SIZE = " + files.size());

        for (MultipartFile file : files) {
            try {
                System.out.println("PROCESS FILE = " + file.getOriginalFilename());

                String url = gcsService.upload(file, "products/" + productId);
                System.out.println("GCS URL = " + url);

                ProductImage image = ProductImage.builder()
                        .product(product)
                        .imageUrl(url)
                        .createdAt(LocalDateTime.now())
                        .build();

                imageRepository.save(image);
                System.out.println("SAVED IMAGE ID = " + image.getId());

                responses.add(mapper.toResponse(image));

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("UPLOAD FAILED", e);
            }
        }
        return responses;
    }

    @Override
    public List<ProductImageResponse> getByProduct(UUID productId) {
        return imageRepository.findByProductId(productId)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @Override
    public void deleteImages(UUID imageId) {

        ProductImage image = imageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        gcsService.deleteByUrl(image.getImageUrl());

        imageRepository.deleteById(imageId);
    }
}
