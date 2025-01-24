package com.example.bookstore.controller;

import com.example.bookstore.mapper.UserMapper;
import com.example.bookstore.mapper.WishListMapper;
import com.example.bookstore.requestdto.WishListRequest;
import com.example.bookstore.responsedto.WishListResponse;
import com.example.bookstore.service.WishListService;
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
@RequestMapping("/wishlist")
@AllArgsConstructor
public class WishListController
{
    private WishListService wishListService;
    private UserMapper userMapper;
    private final WishListMapper wishListMapper=new WishListMapper();
    private static final String HEADER="Authorization";

    @PostMapping("/addToWishList")
    public ResponseEntity<ResponseStructure<WishListResponse>> addToWishList(
            @RequestHeader(value = HEADER)String authHeader,
            @Valid @RequestBody WishListRequest wishListRequest)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails == null || !userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return wishListMapper.headerError();
        }
        return wishListService.addToWishList(userDetails.getUsername(), wishListRequest);
    }


    @GetMapping("/getWishList")
    public ResponseEntity<ResponseStructure<List<WishListResponse>>> getWishList(@RequestHeader(value = HEADER)String authHeader)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if (userDetails == null || !userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return wishListMapper.noAuthority();
        }
        return wishListService.getWishList(userDetails.getUsername());
    }


    @GetMapping("/isInWishList/{bookId}")
    public ResponseEntity<ResponseStructure<Boolean>> inWishList(
            @RequestHeader("Authorization")String authHeader,
            @PathVariable Long bookId)
    {
        UserDetails userDetails=userMapper.validateUserToken(authHeader);
        if(userDetails!=null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return wishListService.isInWishList(userDetails.getUsername(), bookId);
        }
        return wishListMapper.noAuthorityForUser();
    }
}
