package com.example.demo.mapper;

import com.example.demo.entity.Address;
import com.example.demo.entity.User;
import com.example.demo.requestdto.AddressRequest;
import com.example.demo.responsedto.AddressResponse;
import com.example.demo.util.ResponseStructure;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class AddressMapper
{
    public ResponseEntity<ResponseStructure<AddressResponse>> noAuthority()
    {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseStructure<AddressResponse>()
                .setMessage("No Authority")
                .setStatus(HttpStatus.UNAUTHORIZED.value())
                .setData(null));
    }

    public ResponseEntity<ResponseStructure<List<AddressResponse>>> noAuthorityForAllAddress()
    {
        return  ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseStructure<List<AddressResponse>>()
                .setMessage("No Authority")
                .setStatus(HttpStatus.UNAUTHORIZED.value())
                .setData(null));
    }

    public Address mapAddressRequestToAddress(User user, AddressRequest addressRequest)
    {
        return Address.builder()
                .userId(user.getUserId())
                .streetName(addressRequest.getStreetName())
                .city(addressRequest.getCity())
                .state(addressRequest.getState())
                .pinCode(addressRequest.getPinCode()).build();
    }

    public AddressResponse mapAddressToAddressResponse(Address savedAddress)
    {
        return AddressResponse.builder()
                .addressId(savedAddress.getAddressId())
                .streetName(savedAddress.getStreetName())
                .city(savedAddress.getCity())
                .state(savedAddress.getState())
                .pinCode(savedAddress.getPinCode()).build();
    }

    public Address mapOldAddressToNewAddress(Address address, AddressRequest addressRequest)
    {
        address.setStreetName(addressRequest.getStreetName());
        address.setCity(addressRequest.getCity());
        address.setState(addressRequest.getState());
        address.setPinCode(addressRequest.getPinCode());
        return address;
    }

    public ResponseEntity<ResponseStructure<AddressResponse>> mapToSuccessAddAddress(Address savedAddress)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseStructure<AddressResponse>()
                .setStatus(HttpStatus.CREATED.value())
                .setMessage("Address added successfully")
                .setData(mapAddressToAddressResponse(savedAddress)));
    }

    public ResponseEntity<ResponseStructure<AddressResponse>> mapToSuccessUpdateAddress(Address saved, String email)
    {
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<AddressResponse>()
                .setStatus(HttpStatus.OK.value())
                .setMessage("Address with id "+saved.getAddressId()+" updated successfully for the user "+email)
                .setData(mapAddressToAddressResponse(saved)));
    }

    public ResponseEntity<ResponseStructure<AddressResponse>> mapToSuccessGetAddressById(Address resultAddress)
    {
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<AddressResponse>()
                .setMessage("Address fetched successfully")
                .setData(mapAddressToAddressResponse(resultAddress))
                .setStatus(HttpStatus.OK.value()));
    }

    public ResponseEntity<ResponseStructure<List<AddressResponse>>> mapToSuccessGetAllAddress(List<AddressResponse> responseDtoList)
    {
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<List<AddressResponse>>()
                .setStatus(HttpStatus.OK.value())
                .setData(responseDtoList)
                .setMessage("User AddressList fetched successfully"));
    }

    public ResponseEntity<ResponseStructure<AddressResponse>> mapToSuccessDeleteAddress()
    {
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<AddressResponse>()
                .setMessage("Address deleted successfully")
                .setData(null)
                .setStatus(HttpStatus.OK.value()));
    }

    public ResponseEntity<ResponseStructure<List<AddressResponse>>> mapToNoContentForGetAllAddress()
    {
        return ResponseEntity.noContent().build();
    }
}
