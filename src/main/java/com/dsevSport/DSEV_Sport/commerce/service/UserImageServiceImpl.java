package com.dsevSport.DSEV_Sport.commerce.service;


import com.dsevSport.DSEV_Sport.commerce.dto.request.UserImageRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.UserImageResponse;
import com.dsevSport.DSEV_Sport.commerce.mapper.UserImageMapper;
import com.dsevSport.DSEV_Sport.commerce.model.User;
import com.dsevSport.DSEV_Sport.commerce.model.UserImage;
import com.dsevSport.DSEV_Sport.commerce.repository.UserImageRepository;
import com.dsevSport.DSEV_Sport.commerce.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserImageServiceImpl implements UserImageService {

    private final UserRepository userRepository;
    private final UserImageRepository userImageRepository;
    private final GcsService gcsService;

    @Override
    public UserImageResponse uploadUserImage(String username, MultipartFile file) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String url = gcsService.upload(file, "user-images/" + user.getId());

        UserImage entity = new UserImage();
        entity.setUser(user);
        entity.setImageUrl(url);
        entity.setSource("TRYON");
        entity.setIsPrimary(false);
        entity.setCreatedAt(LocalDateTime.now());

        userImageRepository.save(entity);
        return UserImageMapper.toResponse(entity);
    }

    @Override
    public List<UserImageResponse> getMyImages(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return userImageRepository.findByUser_IdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(UserImageMapper::toResponse)
                .toList();
    }

    @Override
    public void deleteMyImage(String username, UUID imageId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserImage image = userImageRepository.findByIdAndUser_Id(imageId, user.getId())
                .orElseThrow(() -> new RuntimeException("Image not found"));

        // xóa trên GCS
        gcsService.deleteByUrl(image.getImageUrl());

        userImageRepository.delete(image);

    }

    @Override
    public void setPrimaryImage(String username, UUID imageId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserImage image = userImageRepository.findByIdAndUser_Id(imageId, user.getId())
                .orElseThrow(() -> new RuntimeException("Image not found"));

        userImageRepository.findByUser_IdAndIsPrimaryTrue(user.getId())
                .forEach(img -> img.setIsPrimary(false));

        image.setIsPrimary(true);
    }
}
