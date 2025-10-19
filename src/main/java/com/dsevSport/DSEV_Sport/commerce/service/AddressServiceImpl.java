package com.dsevSport.DSEV_Sport.commerce.service;

import com.dsevSport.DSEV_Sport.commerce.dto.request.AddressRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.AddressResponse;
import com.dsevSport.DSEV_Sport.commerce.model.Address;
import com.dsevSport.DSEV_Sport.commerce.model.User;
import com.dsevSport.DSEV_Sport.commerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AddressServiceImpl implements AddressService {
    private final UserRepository userRepo;
    @Override
    public List<AddressResponse> getAddresses(String username) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));
        return user.getAddresses().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AddressResponse addAddress(String username, AddressRequest request) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        Address address = Address.builder()
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .city(request.getCity())
                .ward(request.getWard())
                .street(request.getStreet())
                .createdAt(LocalDateTime.now())
                .build();
        user.addAddress(address);
        userRepo.save(user);

        return mapToResponse(address);
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(String username, UUID addressId, AddressRequest request) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        Address address = user.getAddresses().stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Address not found with id: " + addressId));

        address.setFullName(request.getFullName());
        address.setPhoneNumber(request.getPhoneNumber());
        address.setCity(request.getCity());
        address.setWard(request.getWard());
        address.setStreet(request.getStreet());

        userRepo.save(user);
        return mapToResponse(address);
    }

    @Override
    @Transactional
    public void deleteAddress(String username, UUID addressId) {
        User user = userRepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found with username: " + username));

        Address address = user.getAddresses().stream()
                .filter(a -> a.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Address not found with id: " + addressId));

        user.removeAddress(address);
        userRepo.save(user);
    }

    private AddressResponse mapToResponse(Address address) {
        return AddressResponse.builder()
                .id(address.getId())
                .fullName(address.getFullName())
                .phoneNumber(address.getPhoneNumber())
                .city(address.getCity())
                .ward(address.getWard())
                .street(address.getStreet())
                .build();
    }
}
