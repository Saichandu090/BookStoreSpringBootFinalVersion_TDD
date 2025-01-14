package com.example.demo.controller;

import com.example.demo.mapper.AddressMapper;
import com.example.demo.mapper.BookMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.requestdto.AddressRequestDto;
import com.example.demo.responsedto.AddressResponseDto;
import com.example.demo.service.AddressService;
import com.example.demo.util.ResponseStructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ResponseStructure<AddressResponseDto>> addAddress(@RequestHeader("Authorization")String authHeader, @RequestBody AddressRequestDto addressRequestDto)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails != null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority("USER")))
        {
            return addressService.addAddress(userDetails.getUsername(),addressRequestDto);
        }
        return new ResponseEntity<>(addressMapper.noAuthority(),HttpStatus.UNAUTHORIZED);
    }

    @PutMapping("/editAddress/{addressId}")
    public ResponseEntity<ResponseStructure<AddressResponseDto>> editAddress(@RequestHeader("Authorization")String authHeader,@PathVariable Long addressId,@RequestBody AddressRequestDto addressRequestDto)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails != null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority("USER")))
        {
            return addressService.updateAddress(userDetails.getUsername(),addressId,addressRequestDto);
        }
        return new ResponseEntity<>(addressMapper.noAuthority(),HttpStatus.UNAUTHORIZED);
    }
}
