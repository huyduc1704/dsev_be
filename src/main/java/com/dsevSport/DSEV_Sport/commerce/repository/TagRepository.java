package com.dsevSport.DSEV_Sport.commerce.repository;

import com.dsevSport.DSEV_Sport.commerce.model.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {
    boolean existsByName(String name);
}
