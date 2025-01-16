package com.example.demo.controller;

import com.example.demo.mapper.AddressMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.requestdto.AddressRequestDto;
import com.example.demo.responsedto.AddressResponseDto;
import com.example.demo.service.AddressService;
import com.example.demo.util.ResponseStructure;
import com.example.demo.util.Roles;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/address")
public class AddressController
{
    @Autowired
    private AddressService addressService;

    @Autowired
    private UserMapper userMapper;

    private final AddressMapper addressMapper=new AddressMapper();

    @PostMapping("/addAddress")
    public ResponseEntity<ResponseStructure<AddressResponseDto>> addAddress(@RequestHeader(value = "Authorization")String authHeader,@Valid @RequestBody AddressRequestDto addressRequestDto)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails == null || !userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return new ResponseEntity<>(addressMapper.noAuthority(),HttpStatus.UNAUTHORIZED);
        }
        return addressService.addAddress(userDetails.getUsername(),addressRequestDto);
    }

    @PutMapping("/editAddress/{addressId}")
    public ResponseEntity<ResponseStructure<AddressResponseDto>> updateAddress(@RequestHeader(value = "Authorization")String authHeader,@PathVariable Long addressId,@Valid @RequestBody AddressRequestDto addressRequestDto)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails == null || !userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return new ResponseEntity<>(addressMapper.noAuthority(),HttpStatus.UNAUTHORIZED);
        }
        return addressService.updateAddress(userDetails.getUsername(),addressId,addressRequestDto);
    }

    @GetMapping("/getAddress/{addressId}")
    public ResponseEntity<ResponseStructure<AddressResponseDto>> getAddressById(@RequestHeader(value = "Authorization")String authHeader,@PathVariable Long addressId)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails == null || !userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return new ResponseEntity<>(addressMapper.noAuthority(),HttpStatus.UNAUTHORIZED);
        }
        return addressService.getAddressById(addressId);
    }


    @GetMapping("/getAllAddress")
    public ResponseEntity<ResponseStructure<List<AddressResponseDto>>> getAllAddress(@RequestHeader(value = "Authorization")String authHeader)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails == null || !userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return new ResponseEntity<>(addressMapper.noAuthorityForAllAddress(),HttpStatus.UNAUTHORIZED);
        }
        return addressService.getAllAddress(userDetails.getUsername());
    }

    @DeleteMapping("/deleteAddress/{addressId}")
    public ResponseEntity<ResponseStructure<AddressResponseDto>> deleteAddressById(@RequestHeader(value = "Authorization")String authHeader,@PathVariable Long addressId)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails == null || !userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return new ResponseEntity<>(addressMapper.noAuthority(),HttpStatus.UNAUTHORIZED);
        }
        return addressService.deleteAddress(userDetails.getUsername(),addressId);
    }
}
