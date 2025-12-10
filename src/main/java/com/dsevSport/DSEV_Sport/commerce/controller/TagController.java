package com.dsevSport.DSEV_Sport.commerce.controller;

import com.dsevSport.DSEV_Sport.commerce.dto.request.TagRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.ApiResponse;
import com.dsevSport.DSEV_Sport.commerce.dto.response.TagResponse;
import com.dsevSport.DSEV_Sport.commerce.service.TagService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TagController {

    TagService tagService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TagResponse>>> getAllTags() {
        List<TagResponse> data = tagService.getAll();
        return ResponseEntity.ok(
                ApiResponse.<List<TagResponse>>builder()
                        .data(data)
                        .message("Tags retrieved successfully")
                        .code(200)
                        .build()
        );
    }

    @GetMapping("/page")
    public ResponseEntity<ApiResponse<Page<TagResponse>>> getTagsPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<TagResponse> data = tagService.getPage(pageRequest);

        return ResponseEntity.ok(
                ApiResponse.<Page<TagResponse>>builder()
                        .data(data)
                        .message("Tags page retrieved successfully")
                        .code(200)
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TagResponse>> getTagById(@PathVariable UUID id) {
        TagResponse data = tagService.getById(id);
        return ResponseEntity.ok(
                ApiResponse.<TagResponse>builder()
                        .data(data)
                        .message("Tag retrieved successfully")
                        .code(200)
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    @PostMapping
    public ResponseEntity<ApiResponse<TagResponse>> createTag(@RequestBody TagRequest request) {
        TagResponse data = tagService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<TagResponse>builder()
                        .data(data)
                        .message("Tag created successfully")
                        .code(201)
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TagResponse>> updateTag(
            @PathVariable UUID id,
            @RequestBody TagRequest request
    ) {
        TagResponse data = tagService.update(id, request);
        return ResponseEntity.ok(
                ApiResponse.<TagResponse>builder()
                        .data(data)
                        .message("Tag updated successfully")
                        .code(200)
                        .build()
        );
    }

    @PreAuthorize("hasAnyRole('ADMIN','MODERATOR')")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTag(@PathVariable UUID id) {
        tagService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                ApiResponse.<Void>builder()
                        .data(null)
                        .message("Tag deleted successfully")
                        .code(204)
                        .build()
        );
    }
}
