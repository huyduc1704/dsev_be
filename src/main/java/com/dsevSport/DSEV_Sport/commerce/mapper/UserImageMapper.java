package com.dsevSport.DSEV_Sport.commerce.mapper;

import com.dsevSport.DSEV_Sport.commerce.dto.response.UserImageResponse;
import com.dsevSport.DSEV_Sport.commerce.model.UserImage;

public class UserImageMapper {

    private UserImageMapper() {}

    public static UserImageResponse toResponse(UserImage entity) {
        if (entity == null) return null;

        return UserImageResponse.builder()
                .id(entity.getId())
                .imageUrl(entity.getImageUrl())
                .fileName(entity.getFileName())
                .fileSize(entity.getFileSize())
                .width(entity.getWidth())
                .height(entity.getHeight())
                .format(entity.getFormat())
                .isPrimary(entity.getIsPrimary())
                .source(entity.getSource())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
