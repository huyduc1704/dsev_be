package com.dsevSport.DSEV_Sport.commerce.repository;

import com.dsevSport.DSEV_Sport.commerce.model.UserImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserImageRepository extends JpaRepository<UserImage, UUID> {
    List<UserImage> findByUser_IdOrderByCreatedAtDesc(UUID userId);

    Optional<UserImage> findByIdAndUser_Id(UUID id, UUID userId);

    List<UserImage> findByUser_IdAndIsPrimaryTrue(UUID userId);
}
