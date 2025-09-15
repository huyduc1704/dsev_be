package com.dsevSport.DSEV_Sport.commerce.service;

import com.dsevSport.DSEV_Sport.commerce.dto.request.LoginRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.request.RegisterRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.AuthenticationResponse;
import com.dsevSport.DSEV_Sport.commerce.model.Address;
import com.dsevSport.DSEV_Sport.commerce.model.User;
import com.dsevSport.DSEV_Sport.commerce.repository.UserRepository;
import com.dsevSport.DSEV_Sport.common.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public AuthenticationResponse register(RegisterRequest registerRequest) {
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                .phoneNumber(registerRequest.getPhoneNumber())
                .role(registerRequest.getRole())
                .isActive(true)
                .build();
        if (registerRequest.getAddresses() != null) {
            registerRequest.getAddresses().forEach(reqAddress -> {
                Address address = new Address();
                address.setFullName(reqAddress.getFullName());
                address.setPhoneNumber(reqAddress.getPhoneNumber());
                address.setCity(reqAddress.getCity());
                address.setWard(reqAddress.getWard());
                address.setStreet(reqAddress.getStreet());
                address.setCreatedAt(LocalDateTime.now());
                user.addAddress(address);
            });
        }
        log.info("Start register for {}", registerRequest.getUsername());
        userRepository.saveAndFlush(user);
        log.info("Saved user {}, generating token...", user.getUsername());
        String token = jwtService.generateToken(user);
        log.info("Generated token for {}", user.getUsername());

        return AuthenticationResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .accessToken(token)
                .build();

    }

    @Override
    public AuthenticationResponse login(LoginRequest loginRequest) {
        var auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );
        User user = (User) auth.getPrincipal();

        String token = jwtService.generateToken(user);

        return AuthenticationResponse.builder()
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().name())
                .accessToken(token)
                .build();
    }
}
