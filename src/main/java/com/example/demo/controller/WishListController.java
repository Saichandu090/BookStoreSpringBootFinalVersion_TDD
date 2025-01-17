package com.example.demo.controller;

import com.example.demo.mapper.UserMapper;
import com.example.demo.mapper.WishListMapper;
import com.example.demo.requestdto.WishListRequestDto;
import com.example.demo.responsedto.WishListResponseDto;
import com.example.demo.service.WishListService;
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
@RequestMapping("/wishlist")
@AllArgsConstructor
public class WishListController
{
    private WishListService wishListService;
    private UserMapper userMapper;
    private final WishListMapper wishListMapper=new WishListMapper();

    @PostMapping("/addToWishList")
    public ResponseEntity<ResponseStructure<WishListResponseDto>> addToWishList(@RequestHeader(value = "Authorization")String authHeader,@Valid @RequestBody WishListRequestDto wishListRequestDto)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails == null || !userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return new ResponseEntity<>(wishListMapper.headerError(), HttpStatus.UNAUTHORIZED);
        }
        return wishListService.addToWishList(userDetails.getUsername(),wishListRequestDto);
    }

    @GetMapping("/getWishList")
    public ResponseEntity<ResponseStructure<List<WishListResponseDto>>> getWishList(@RequestHeader(value = "Authorization")String authHeader)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails == null || !userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return new ResponseEntity<>(wishListMapper.noAuthority(), HttpStatus.UNAUTHORIZED);
        }
        return wishListService.getWishList(userDetails.getUsername());
    }
}
