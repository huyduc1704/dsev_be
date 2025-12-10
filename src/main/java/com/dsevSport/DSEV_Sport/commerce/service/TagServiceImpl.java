package com.dsevSport.DSEV_Sport.commerce.service;

import com.dsevSport.DSEV_Sport.commerce.dto.request.TagRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.TagResponse;
import com.dsevSport.DSEV_Sport.commerce.mapper.TagMapper;
import com.dsevSport.DSEV_Sport.commerce.model.Tag;
import com.dsevSport.DSEV_Sport.commerce.repository.TagRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TagServiceImpl implements TagService {

    TagRepository tagRepository;
    TagMapper tagMapper;

    @Override
    public TagResponse create(TagRequest tagRequest) {
        if (tagRepository.existsByName(tagRequest.getName())) {
            throw new RuntimeException("Tag name already exists");
        }
        Tag tag = tagMapper.toEntity(tagRequest);
        Tag saved = tagRepository.save(tag);
        return tagMapper.toResponse(saved);
    }

    @Override
    public TagResponse update(UUID id, TagRequest tagRequest) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found"));

        if (tagRequest.getName() != null
        && !tagRequest.getName().equals(tag.getName())
        && tagRepository.existsByName(tagRequest.getName())) {
            throw new RuntimeException("Tag name already exists");
        }

        tagMapper.updateEntity(tagRequest, tag);
        Tag saved = tagRepository.save(tag);
        return tagMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found"));
        tagRepository.delete(tag);
    }

    @Override
    public TagResponse getById(UUID id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tag not found"));
        return tagMapper.toResponse(tag);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> getAll() {
        return tagMapper.toResponseList(tagRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TagResponse> getPage(Pageable pageable) {
        return tagRepository.findAll(pageable).map(tagMapper::toResponse);
    }
}
