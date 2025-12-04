package com.dsevSport.DSEV_Sport.commerce.service;

import com.dsevSport.DSEV_Sport.commerce.dto.request.TryonSessionRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.TryonSessionResponse;
import com.dsevSport.DSEV_Sport.commerce.mapper.TryonSessionMapper;
import com.dsevSport.DSEV_Sport.commerce.model.*;
import com.dsevSport.DSEV_Sport.commerce.repository.*;
import com.dsevSport.DSEV_Sport.tryon.client.FalCatVtonClient;
import com.dsevSport.DSEV_Sport.tryon.client.FalCatVtonResult;
import com.dsevSport.DSEV_Sport.tryon.config.FalProperties;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class TryonSessionServiceImpl implements TryonSessionService {

    private final UserRepository userRepository;
    private final ProductImageRepository productImageRepository;
    private final UserImageRepository userImageRepository;
    private final TryonSessionRepository tryonSessionRepository;
    private final FalCatVtonClient falCatVtonClient;
    private final FalProperties falProperties;

    @Override
    public TryonSessionResponse createTryonSession(String username, TryonSessionRequest request) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        ProductImage productImage = productImageRepository
                .findById(request.getProductImageId())
                .orElseThrow(() -> new RuntimeException("Product image not found"));

        UserImage inputImage = userImageRepository
                .findByIdAndUser_Id(request.getUserImageId(), user.getId())
                .orElseThrow(() -> new RuntimeException("User image not found / unauthorized"));

        TryonSession session = new TryonSession();
        session.setUser(user);
        session.setProductImage(productImage);
        session.setInputImage(inputImage);
        session.setImageInputUrl(inputImage.getImageUrl());
        session.setStatus("PROCESSING");
        session.setModelName(falProperties.getCatVtonModelName());
        session.setRetryCount(0);
        session.setCreatedAt(LocalDateTime.now());

        tryonSessionRepository.save(session);

        // 🔥 GỌI FAL.AI
        FalCatVtonResult result = falCatVtonClient.generate(
                inputImage.getImageUrl(),
                productImage.getImageUrl()
        );

        // 🔥 LOG RESPONSE
        log.info("FAL RESULT status={}, requestId={}, output={}",
                result.getStatus(),
                result.getRequestId(),
                result.getOutputImageUrl()
        );

        session.setRequestId(result.getRequestId());
        session.setEstimatedCost(result.getEstimatedCost());

        // ✅ CASE 1: HOÀN THÀNH
        if ("COMPLETED".equalsIgnoreCase(result.getStatus())
                && result.getOutputImageUrl() != null) {

            session.setOutputResultUrl(result.getOutputImageUrl());
            session.setStatus("COMPLETED");
            session.setFinishedAt(LocalDateTime.now());
            session.setFailReason(null);
        }

        // ⏳ CASE 2: CHƯA XONG
        else if ("QUEUED".equalsIgnoreCase(result.getStatus())) {

            session.setStatus("PROCESSING");
            session.setFailReason("Fal.ai is processing job. Try again later.");
        }

        // ❌ CASE 3: FAILED
        else {

            String reason = "FAL FAILED: " + result.getRawResponse();
            session.setStatus("FAILED");
            session.setFailReason(reason);
            session.setFinishedAt(LocalDateTime.now());

            log.error("Try-on FAILED: {}", reason);
        }

        return TryonSessionMapper.toResponse(session);
    }

    @Override
    public TryonSessionResponse getMyTryonSession(String username, UUID sessionId) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TryonSession session = tryonSessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (!session.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        return TryonSessionMapper.toResponse(session);
    }


    @Override
    public List<TryonSessionResponse> getMySessions(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return tryonSessionRepository
                .findByUser_IdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(TryonSessionMapper::toResponse)
                .toList();
    }
}
