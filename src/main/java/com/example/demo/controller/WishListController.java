package com.example.demo.controller;

import com.example.demo.mapper.UserMapper;
import com.example.demo.mapper.WishListMapper;
import com.example.demo.requestdto.WishListRequestDto;
import com.example.demo.responsedto.WishListResponseDto;
import com.example.demo.service.WishListService;
import com.example.demo.util.ResponseStructure;
import com.example.demo.util.Roles;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/wishlist")
public class WishListController
{
    @Autowired
    private WishListService wishListService;

    @Autowired
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

    @DeleteMapping("/removeFromWishList/{bookId}")
    public ResponseEntity<ResponseStructure<WishListResponseDto>> removeFromWishList(@RequestHeader(value = "Authorization")String authHeader, @PathVariable Long bookId)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails == null || !userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return new ResponseEntity<>(wishListMapper.headerError(), HttpStatus.UNAUTHORIZED);
        }
        return wishListService.removeFromWishList(userDetails.getUsername(),bookId);
    }
}
