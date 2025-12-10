package com.dsevSport.DSEV_Sport.commerce.service;

import com.dsevSport.DSEV_Sport.commerce.dto.request.TagRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.TagResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface TagService {
    TagResponse create(TagRequest tagRequest);
    TagResponse update(UUID id, TagRequest tagRequest);
    void delete(UUID id);
    TagResponse getById(UUID id);
    List<TagResponse> getAll();
    Page<TagResponse> getPage(Pageable pageable);
}
