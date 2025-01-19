package com.example.demo.controller;

import com.example.demo.mapper.CartMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.requestdto.CartRequestDto;
import com.example.demo.responsedto.CartResponseDto;
import com.example.demo.service.CartService;
import com.example.demo.util.ResponseStructure;
import com.example.demo.util.Roles;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(allowedHeaders = "*",origins = "*")
@RestController
@RequestMapping("/cart")
@AllArgsConstructor
public class CartController
{
    private CartService cartService;
    private UserMapper userMapper;
    private final CartMapper cartMapper=new CartMapper();
    private static final String HEADER="Authorization";


    @PostMapping("/addToCart")
    public ResponseEntity<ResponseStructure<CartResponseDto>> addToCart(
            @RequestHeader(value = HEADER)String authHeader,
            @Valid @RequestBody CartRequestDto cartRequestDto)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if(userDetails != null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return cartService.addToCart(userDetails.getUsername(),cartRequestDto);
        }
        return cartMapper.noAuthority();
    }


    @DeleteMapping("/removeFromCart/{cartId}")
    public ResponseEntity<ResponseStructure<CartResponseDto>> removeFromCart(
            @RequestHeader(value = HEADER)String authHeader,
            @PathVariable Long cartId)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if(userDetails != null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return cartService.removeFromCart(userDetails.getUsername(),cartId);
        }
        return cartMapper.noAuthority();
    }


    @DeleteMapping("/clearCart")
    public ResponseEntity<ResponseStructure<CartResponseDto>> clearCart(@RequestHeader(value = HEADER)String authHeader)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if(userDetails != null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return cartService.clearCart(userDetails.getUsername());
        }
        return cartMapper.noAuthority();
    }


    @GetMapping("/getCart")
    public ResponseEntity<ResponseStructure<List<CartResponseDto>>> getCartItems(@RequestHeader(value = HEADER)String authHeader)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if(userDetails != null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return cartService.getCartItems(userDetails.getUsername());
        }
        return cartMapper.noAuthority("No Authority");
    }
}
