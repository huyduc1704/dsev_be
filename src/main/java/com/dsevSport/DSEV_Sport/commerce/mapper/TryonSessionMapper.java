package com.dsevSport.DSEV_Sport.commerce.mapper;

import com.dsevSport.DSEV_Sport.commerce.dto.response.TryonSessionResponse;
import com.dsevSport.DSEV_Sport.commerce.model.*;

public class TryonSessionMapper {
    private TryonSessionMapper() {
    }

    public static TryonSessionResponse toResponse(TryonSession session) {
        if (session == null) return null;

        User user = session.getUser();
        ProductImage productImage = session.getProductImage();
        UserImage inputImage = session.getInputImage();

        return TryonSessionResponse.builder()
                .id(session.getId())
                .userId(user != null ? user.getId() : null)
                .productImageId(productImage != null ? productImage.getId() : null)
                .inputImageId(inputImage != null ? inputImage.getId() : null)
                .imageInputUrl(session.getImageInputUrl())
                .outputResultUrl(session.getOutputResultUrl())
                .status(session.getStatus())
                .deviceInfo(session.getDeviceInfo())
                .modelName(session.getModelName())
                .prompt(session.getPrompt())
                .requestId(session.getRequestId())
                .estimatedCost(session.getEstimatedCost())
                .createdAt(session.getCreatedAt())
                .finishedAt(session.getFinishedAt())
                .failReason(session.getFailReason())
                .retryCount(session.getRetryCount())
                .build();
    }
}
