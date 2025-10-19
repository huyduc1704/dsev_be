package com.dsevSport.DSEV_Sport.commerce.service;

import com.dsevSport.DSEV_Sport.commerce.dto.request.UpdateProfileRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.AddressResponse;
import com.dsevSport.DSEV_Sport.commerce.dto.response.UserProfileResponse;
import com.dsevSport.DSEV_Sport.commerce.model.User;
import com.dsevSport.DSEV_Sport.commerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepo;
    @Override
    public UserProfileResponse getProfile(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
        return mapToProfileResponse(user);
    }

    @Override
    public UserProfileResponse updateProfile(String username, UpdateProfileRequest request) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));

        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            user.setEmail(request.getEmail());
        }

        if (request.getPhoneNumber() != null && !request.getPhoneNumber().isBlank()) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        userRepo.save(user);
        return mapToProfileResponse(user);
    }

    private UserProfileResponse mapToProfileResponse(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole().name())
                .addresses(user.getAddresses().stream()
                        .map(addr -> AddressResponse.builder()
                                .id(addr.getId())
                                .fullName(addr.getFullName())
                                .phoneNumber(addr.getPhoneNumber())
                                .city(addr.getCity())
                                .ward(addr.getWard())
                                .street(addr.getStreet())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}
