package com.example.demo.service;

import com.example.demo.requestdto.AddressRequestDto;
import com.example.demo.responsedto.AddressResponseDto;
import com.example.demo.util.ResponseStructure;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface AddressService
{
    ResponseEntity<ResponseStructure<AddressResponseDto>> addAddress(String email, AddressRequestDto addressRequestDto);

    ResponseEntity<ResponseStructure<AddressResponseDto>> updateAddress(String email,Long addressId,AddressRequestDto addressRequestDto);

    ResponseEntity<ResponseStructure<AddressResponseDto>> getAddressById(String email,Long addressId);

    ResponseEntity<ResponseStructure<List<AddressResponseDto>>> getAllAddress(String email);

    ResponseEntity<ResponseStructure<AddressResponseDto>> deleteAddress(String email,Long addressId);
}
