package com.dsevSport.DSEV_Sport.commerce.mapper;

import com.dsevSport.DSEV_Sport.commerce.dto.request.TagRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.TagResponse;
import com.dsevSport.DSEV_Sport.commerce.model.Tag;
import org.springframework.stereotype.Component;

@Component
public class TagMapper implements CrudMapper<Tag, TagResponse, TagRequest, TagRequest> {


    @Override
    public TagResponse toResponse(Tag entity) {
        if (entity == null) return null;
        return TagResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .displayName(entity.getDisplayName())
                .build();
    }

    @Override
    public Tag toEntity(TagRequest req) {
        if (req == null) return null;
        return Tag.builder()
                .name(req.getName())
                .displayName(req.getDisplayName())
                .build();
    }

    @Override
    public void updateEntity(TagRequest req, Tag entity) {
        if (req == null || entity == null) return;
        setIfNotNull(req.getName(), entity::setName);
        setIfNotNull(req.getDisplayName(), entity::setDisplayName);
    }


}
