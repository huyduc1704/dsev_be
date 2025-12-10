package com.dsevSport.DSEV_Sport.commerce.service;

import com.dsevSport.DSEV_Sport.commerce.dto.request.CategoryRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.CategoryResponse;
import com.dsevSport.DSEV_Sport.commerce.mapper.CrudMapper;
import com.dsevSport.DSEV_Sport.commerce.model.Category;
import com.dsevSport.DSEV_Sport.commerce.repository.CategoryRepository;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CrudMapper<Category, CategoryResponse, CategoryRequest, CategoryRequest> categoryMapper;
    private final Storage storage;

    @Value("${gcs.bucket}")
    private String bucketName;

    @Override
    public List<CategoryResponse> getAllCategories() {
        return categoryMapper.toResponseList(categoryRepository.findAll());
    }

    @Override
    public CategoryResponse getCategoryById(UUID id) {
        return categoryRepository.findById(id)
                .map(categoryMapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
    }

    @Override
    public CategoryResponse createCategory(CategoryRequest request) {
        log.info("Service.createCategory called with name: {}", request.getName());

        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException("Category name already exists: " + request.getName());
        }
        Category category = categoryMapper.toEntity(request);

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    public CategoryResponse updateCategory(UUID id, CategoryRequest request) {
        log.info("Service.updateCategory called with id: {}, name: {}", id, request.getName());

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
            throw new IllegalArgumentException("Category name already exists: " + request.getName());
        }
        categoryMapper.updateEntity(request, category);

        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    @Override
    public void deleteCategory(UUID id) {
        if (!categoryRepository.existsById(id)) {
            throw new IllegalArgumentException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }

    @Override
    public CategoryResponse uploadCategoryImage(UUID id, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with id: " + id));
        category.setImageUrl(uploadImage(image));
        return categoryMapper.toResponse(categoryRepository.save(category));
    }

    private String uploadImage(MultipartFile image) {
        if (!StringUtils.hasText(bucketName)) {
            throw new IllegalStateException("GCS bucket name is not configured");
        }
        try {
            String extension = StringUtils.getFilenameExtension(image.getOriginalFilename());
            String objectName = "categories/" + UUID.randomUUID() + (extension != null ? "." + extension : "");
            BlobId blobId = BlobId.of(bucketName, objectName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(image.getContentType())
                    .build();
            storage.create(blobInfo, image.getBytes());
            return "https://storage.googleapis.com/" + bucketName + "/" + objectName;
        } catch (IOException ex) {
            log.error("Failed to upload category image", ex);
            throw new IllegalStateException("Failed to upload category image", ex);
        }
    }
}
