package com.example.demo.controller;

import com.example.demo.mapper.AddressMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.requestdto.AddressRequestDto;
import com.example.demo.responsedto.AddressResponseDto;
import com.example.demo.service.AddressService;
import com.example.demo.util.ResponseStructure;
import com.example.demo.util.Roles;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<ResponseStructure<AddressResponseDto>> addAddress(@RequestHeader(value = HEADER)String authHeader,@Valid @RequestBody AddressRequestDto addressRequestDto)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails == null || !userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return new ResponseEntity<>(addressMapper.noAuthority(),HttpStatus.UNAUTHORIZED);
        }
        return addressService.addAddress(userDetails.getUsername(),addressRequestDto);
    }

    @PutMapping("/editAddress/{addressId}")
    public ResponseEntity<ResponseStructure<AddressResponseDto>> updateAddress(@RequestHeader(value = HEADER)String authHeader,@PathVariable Long addressId,@Valid @RequestBody AddressRequestDto addressRequestDto)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails == null || !userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return new ResponseEntity<>(addressMapper.noAuthority(),HttpStatus.UNAUTHORIZED);
        }
        return addressService.updateAddress(userDetails.getUsername(),addressId,addressRequestDto);
    }

    @GetMapping("/getAddress/{addressId}")
    public ResponseEntity<ResponseStructure<AddressResponseDto>> getAddressById(@RequestHeader(value = HEADER)String authHeader,@PathVariable Long addressId)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails == null || !userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return new ResponseEntity<>(addressMapper.noAuthority(),HttpStatus.UNAUTHORIZED);
        }
        return addressService.getAddressById(userDetails.getUsername(),addressId);
    }


    @GetMapping("/getAllAddress")
    public ResponseEntity<ResponseStructure<List<AddressResponseDto>>> getAllAddress(@RequestHeader(value = HEADER)String authHeader)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails == null || !userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return new ResponseEntity<>(addressMapper.noAuthorityForAllAddress(),HttpStatus.UNAUTHORIZED);
        }
        return addressService.getAllAddress(userDetails.getUsername());
    }

    @DeleteMapping("/deleteAddress/{addressId}")
    public ResponseEntity<ResponseStructure<AddressResponseDto>> deleteAddressById(@RequestHeader(value = HEADER)String authHeader,@PathVariable Long addressId)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails == null || !userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return new ResponseEntity<>(addressMapper.noAuthority(),HttpStatus.UNAUTHORIZED);
        }
        return addressService.deleteAddress(userDetails.getUsername(),addressId);
    }
}
