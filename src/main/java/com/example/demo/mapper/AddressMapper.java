package com.example.demo.mapper;

import com.example.demo.entity.Address;
import com.example.demo.entity.User;
import com.example.demo.requestdto.AddressRequestDto;
import com.example.demo.responsedto.AddressResponseDto;
import com.example.demo.util.ResponseStructure;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class AddressMapper
{
    public ResponseStructure<AddressResponseDto> noAuthority()
    {
        return new ResponseStructure<AddressResponseDto>()
                .setMessage("No Authority")
                .setStatus(HttpStatus.UNAUTHORIZED.value())
                .setData(null);
    }

    public ResponseStructure<List<AddressResponseDto>> noAuthorityForAllAddress()
    {
        return new ResponseStructure<List<AddressResponseDto>>()
                .setMessage("No Authority")
                .setStatus(HttpStatus.UNAUTHORIZED.value())
                .setData(null);
    }

    public Address mapAddressRequestToAddress(User user, AddressRequestDto addressRequestDto)
    {
        return Address.builder()
                .user(user)
                .streetName(addressRequestDto.getStreetName())
                .city(addressRequestDto.getCity())
                .state(addressRequestDto.getState())
                .pinCode(addressRequestDto.getPinCode()).build();
    }

    public AddressResponseDto mapAddressToAddressResponse(Address savedAddress)
    {
        return AddressResponseDto.builder()
                .addressId(savedAddress.getAddressId())
                .streetName(savedAddress.getStreetName())
                .city(savedAddress.getCity())
                .state(savedAddress.getState())
                .pinCode(savedAddress.getPinCode()).build();
    }

    public Address mapOldAddressToNewAddress(Address address, AddressRequestDto addressRequestDto)
    {
        address.setStreetName(addressRequestDto.getStreetName());
        address.setCity(addressRequestDto.getCity());
        address.setState(addressRequestDto.getState());
        address.setPinCode(addressRequestDto.getPinCode());
        return address;
    }

    public ResponseEntity<ResponseStructure<AddressResponseDto>> mapToSuccessAddAddress(Address savedAddress)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseStructure<AddressResponseDto>()
                .setStatus(HttpStatus.CREATED.value())
                .setMessage("Address added successfully")
                .setData(mapAddressToAddressResponse(savedAddress)));
    }

    public ResponseEntity<ResponseStructure<AddressResponseDto>> mapToSuccessUpdateAddress(Address saved,String email)
    {
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<AddressResponseDto>()
                .setStatus(HttpStatus.OK.value())
                .setMessage("Address with id "+saved.getAddressId()+" updated successfully for the user "+email)
                .setData(mapAddressToAddressResponse(saved)));
    }

    public ResponseEntity<ResponseStructure<AddressResponseDto>> mapToSuccessGetAddressById(Address resultAddress)
    {
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<AddressResponseDto>()
                .setMessage("Address fetched successfully")
                .setData(mapAddressToAddressResponse(resultAddress))
                .setStatus(HttpStatus.OK.value()));
    }

    public ResponseEntity<ResponseStructure<List<AddressResponseDto>>> mapToSuccessGetAllAddress(List<AddressResponseDto> responseDtoList)
    {
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<List<AddressResponseDto>>()
                .setStatus(HttpStatus.OK.value())
                .setData(responseDtoList)
                .setMessage("User AddressList fetched successfully"));
    }

    public ResponseEntity<ResponseStructure<AddressResponseDto>> mapToSuccessDeleteAddress()
    {
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<AddressResponseDto>()
                .setMessage("Address deleted successfully")
                .setData(null)
                .setStatus(HttpStatus.OK.value()));
    }
}
