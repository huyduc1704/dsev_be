package com.dsevSport.DSEV_Sport.commerce.service;

import com.dsevSport.DSEV_Sport.commerce.dto.request.AddressRequest;
import com.dsevSport.DSEV_Sport.commerce.dto.response.AddressResponse;

import java.util.List;
import java.util.UUID;

public interface AddressService {
    List<AddressResponse> getAddresses(String username);
    AddressResponse addAddress(String username, AddressRequest request);
    AddressResponse updateAddress(String username, UUID addressId, AddressRequest request);
    void deleteAddress(String username, UUID addressId);
}
