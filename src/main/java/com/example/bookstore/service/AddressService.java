package com.example.bookstore.service;

import com.example.bookstore.requestdto.AddressRequest;
import com.example.bookstore.responsedto.AddressResponse;
import com.example.bookstore.util.ResponseStructure;
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
