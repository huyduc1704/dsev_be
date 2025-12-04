package com.dsevSport.DSEV_Sport.commerce.service;

import com.dsevSport.DSEV_Sport.commerce.dto.request.UserImageRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.UserImageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface UserImageService {

    UserImageResponse uploadUserImage(String username,
                                      MultipartFile file);

    List<UserImageResponse> getMyImages(String username);

    void deleteMyImage(String username, UUID imageId);

    void setPrimaryImage(String username, UUID imageId);
}
