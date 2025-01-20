package com.example.bookstore.controller;

import com.example.bookstore.mapper.AddressMapper;
import com.example.bookstore.mapper.UserMapper;
import com.example.bookstore.requestdto.AddressRequest;
import com.example.bookstore.responsedto.AddressResponse;
import com.example.bookstore.service.AddressService;
import com.example.bookstore.util.ResponseStructure;
import com.example.bookstore.util.Roles;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(allowedHeaders = "*",origins = "*")
@RestController
@RequestMapping("/address")
@AllArgsConstructor
public class AddressController
{
    private AddressService addressService;
    private UserMapper userMapper;
    private static final String HEADER="Authorization";
    private final AddressMapper addressMapper=new AddressMapper();


    @PostMapping("/addAddress")
    public ResponseEntity<ResponseStructure<AddressResponse>> addAddress(
            @RequestHeader(value = HEADER)String authHeader,
            @Valid @RequestBody AddressRequest addressRequest)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails == null || !userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return addressMapper.noAuthority();
        }
        return addressService.addAddress(userDetails.getUsername(), addressRequest);
    }


    @PutMapping("/editAddress/{addressId}")
    public ResponseEntity<ResponseStructure<AddressResponse>> updateAddress(
            @RequestHeader(value = HEADER)String authHeader,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest addressRequest)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails == null || !userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return addressMapper.noAuthority();
        }
        return addressService.updateAddress(userDetails.getUsername(),addressId, addressRequest);
    }


    @GetMapping("/getAddress/{addressId}")
    public ResponseEntity<ResponseStructure<AddressResponse>> getAddressById(
            @RequestHeader(value = HEADER)String authHeader,
            @PathVariable Long addressId)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails == null || !userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return addressMapper.noAuthority();
        }
        return addressService.getAddressById(userDetails.getUsername(),addressId);
    }


    @GetMapping("/getAllAddress")
    public ResponseEntity<ResponseStructure<List<AddressResponse>>> getAllAddress(@RequestHeader(value = HEADER)String authHeader)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails == null || !userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return addressMapper.noAuthorityForAllAddress();
        }
        return addressService.getAllAddress(userDetails.getUsername());
    }


    @DeleteMapping("/deleteAddress/{addressId}")
    public ResponseEntity<ResponseStructure<AddressResponse>> deleteAddressById(
            @RequestHeader(value = HEADER)String authHeader,
            @PathVariable Long addressId)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails == null || !userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return addressMapper.noAuthority();
        }
        return addressService.deleteAddress(userDetails.getUsername(),addressId);
    }
}
