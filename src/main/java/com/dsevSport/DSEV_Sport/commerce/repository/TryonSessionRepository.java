package com.dsevSport.DSEV_Sport.commerce.repository;

import com.dsevSport.DSEV_Sport.commerce.model.TryonSession;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface TryonSessionRepository extends CrudRepository<TryonSession, UUID> {

    List<TryonSession> findByUser_IdOrderByCreatedAtDesc(UUID userId);
}
