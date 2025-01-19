package com.example.demo.service;

import com.example.demo.requestdto.AddressRequest;
import com.example.demo.responsedto.AddressResponse;
import com.example.demo.util.ResponseStructure;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface AddressService
{
    ResponseEntity<ResponseStructure<AddressResponse>> addAddress(String email, AddressRequest addressRequest);

    ResponseEntity<ResponseStructure<AddressResponse>> updateAddress(String email, Long addressId, AddressRequest addressRequest);

    ResponseEntity<ResponseStructure<AddressResponse>> getAddressById(String email, Long addressId);

    ResponseEntity<ResponseStructure<List<AddressResponse>>> getAllAddress(String email);

    ResponseEntity<ResponseStructure<AddressResponse>> deleteAddress(String email, Long addressId);
}
