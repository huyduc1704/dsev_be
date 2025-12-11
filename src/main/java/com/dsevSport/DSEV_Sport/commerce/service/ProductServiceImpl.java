package com.dsevSport.DSEV_Sport.commerce.service;

import com.dsevSport.DSEV_Sport.commerce.dto.request.ProductRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.ProductResponse;
import com.dsevSport.DSEV_Sport.commerce.mapper.ProductMapper;
import com.dsevSport.DSEV_Sport.commerce.mapper.ProductVariantMapper;
import com.dsevSport.DSEV_Sport.commerce.model.Product;
import com.dsevSport.DSEV_Sport.commerce.model.Tag;
import com.dsevSport.DSEV_Sport.commerce.repository.ProductRepository;
import com.dsevSport.DSEV_Sport.commerce.repository.ProductVariantRepository;
import com.dsevSport.DSEV_Sport.commerce.repository.TagRepository;
import com.dsevSport.DSEV_Sport.commerce.specification.ProductSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {
    private final ProductRepository repository;
    private final ProductMapper mapper;
    private final ProductVariantRepository variantRepository;
    private final ProductVariantMapper variantMapper;
    private final TagRepository tagRepository;

    @Override
    public List<ProductResponse> getAllProducts() {
        List<ProductResponse> list = mapper.toResponseList(repository.findAll());
        list.forEach(p -> p.setVariants(variantMapper.toResponseList(variantRepository.findByProductId(p.getId()))));
        return list;
    }

    @Override
    public ProductResponse getProductById(UUID id) {
        ProductResponse resp = repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));
        resp.setVariants(variantMapper.toResponseList(variantRepository.findByProductId(id)));
        return resp;
    }

    @Override
    public List<ProductResponse> getProductsByCategory(UUID categoryId) {
        List<ProductResponse> list = mapper.toResponseList(repository.findByCategoryId(categoryId));
        list.forEach(p -> p.setVariants(variantMapper.toResponseList(variantRepository.findByProductId(p.getId()))));
        return list;
    }

    @Override
    public List<ProductResponse> searchProductsByName(String name) {
        List<ProductResponse> list = mapper.toResponseList(repository.findByNameContainingIgnoreCase(name));
        list.forEach(p -> p.setVariants(variantMapper.toResponseList(variantRepository.findByProductId(p.getId()))));
        return list;
    }

    @Override
    public List<ProductResponse> getActiveProducts() {
        List<ProductResponse> list = mapper.toResponseList(repository.findByIsActiveTrue());
        list.forEach(p -> p.setVariants(variantMapper.toResponseList(variantRepository.findByProductId(p.getId()))));
        return list;
    }

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Product product = mapper.toEntity(request);
        product.setId(UUID.randomUUID());

        // Handle tags
        if (request.getTagIds() != null && !request.getTagIds().isEmpty()) {
            List<Tag> tags = tagRepository.findAllById(request.getTagIds());
            if (tags.size() != request.getTagIds().size()) {
                throw new IllegalArgumentException("One or more tags not found");
            }
            product.setTags(tags);
        }

        Product savedProduct = repository.save(product);
        ProductResponse resp = mapper.toResponse(savedProduct);
        resp.setVariants(variantMapper.toResponseList(variantRepository.findByProductId(resp.getId())));
        return resp;
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(UUID id, ProductRequest request) {
        Product product = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + id));

        mapper.updateEntity(request, product);

        // Handle tags update
        if (request.getTagIds() != null) {
            if (request.getTagIds().isEmpty()) {
                // Clear all tags
                product.getTags().clear();
            } else {
                // Update tags
                List<Tag> tags = tagRepository.findAllById(request.getTagIds());
                if (tags.size() != request.getTagIds().size()) {
                    throw new IllegalArgumentException("One or more tags not found");
                }
                product.getTags().clear();
                product.getTags().addAll(tags);
            }
        }

        Product savedProduct = repository.save(product);
        ProductResponse resp = mapper.toResponse(savedProduct);
        resp.setVariants(variantMapper.toResponseList(variantRepository.findByProductId(id)));
        return resp;
    }

    @Override
    @Transactional
    public ProductResponse addTagsToProduct(UUID productId, List<UUID> tagIds) {
        Product product = repository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        List<Tag> tagsToAdd = tagRepository.findAllById(tagIds);
        if (tagsToAdd.size() != tagIds.size()) {
            throw new IllegalArgumentException("One or more tags not found");
        }

        // Tránh duplicate tags
        tagsToAdd.forEach(tag -> {
            if (!product.getTags().contains(tag)) {
                product.getTags().add(tag);
            }
        });

        Product savedProduct = repository.save(product);
        ProductResponse resp = mapper.toResponse(savedProduct);
        resp.setVariants(variantMapper.toResponseList(variantRepository.findByProductId(productId)));
        return resp;
    }

    @Override
    @Transactional
    public ProductResponse removeTagFromProduct(UUID productId, UUID tagId) {
        Product product = repository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found"));

        product.getTags().remove(tag);

        Product savedProduct = repository.save(product);
        ProductResponse resp = mapper.toResponse(savedProduct);
        resp.setVariants(variantMapper.toResponseList(variantRepository.findByProductId(productId)));
        return resp;
    }

    @Override
    public void deleteProduct(UUID id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("Product not found with id: " + id);
        }
        repository.deleteById(id);
    }

    @Override
    public Page<ProductResponse> filterProducts(String search, String brand, Double minPrice, Double maxPrice, Boolean active, UUID categoryId, Pageable pageable) {
        Specification<Product> spec = null;

        if (search != null && !search.isBlank()) {
            Specification<Product> s = ProductSpecification.nameOrDescriptionContains(search.trim());
            if (s != null) spec = spec == null ? s : spec.and(s);
        }

        if (brand != null && !brand.isBlank()) {
            Specification<Product> s = ProductSpecification.hasBrand(brand.trim());
            if (s != null) spec = spec == null ? s : spec.and(s);
        }

        if (categoryId != null) {
            Specification<Product> s = ProductSpecification.hasCategory(categoryId);
            if (s != null) spec = spec == null ? s : spec.and(s);
        }

        Specification<Product> priceSpec = ProductSpecification.priceBetween(minPrice, maxPrice);
        if (priceSpec != null) spec = spec == null ? priceSpec : spec.and(priceSpec);

        Specification<Product> activeSpec = ProductSpecification.isActive(active);
        if (activeSpec != null) spec = spec == null ? activeSpec : spec.and(activeSpec);

        return repository.findAll(spec, pageable)
                .map(product -> {
                    ProductResponse resp = mapper.toResponse(product);
                    resp.setVariants(variantMapper.toResponseList(variantRepository.findByProductId(resp.getId())));
                    return resp;
                });
    }
}