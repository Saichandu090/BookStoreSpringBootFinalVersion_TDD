package com.example.demo.service;

import com.example.demo.requestdto.AddressRequestDto;
import com.example.demo.responsedto.AddressResponseDto;
import com.example.demo.util.ResponseStructure;
import org.springframework.http.ResponseEntity;

public interface AddressService
{
    ResponseEntity<ResponseStructure<AddressResponseDto>> addAddress(String email, AddressRequestDto addressRequestDto);

    ResponseEntity<ResponseStructure<AddressResponseDto>> updateAddress(String email,Long addressId,AddressRequestDto addressRequestDto);
}
