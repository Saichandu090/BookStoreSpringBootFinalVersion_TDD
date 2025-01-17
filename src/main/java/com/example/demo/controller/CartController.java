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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cart")
@AllArgsConstructor
public class CartController
{
    private CartService cartService;
    private UserMapper userMapper;
    private final CartMapper cartMapper=new CartMapper();

    @PostMapping("/addToCart")
    public ResponseEntity<ResponseStructure<CartResponseDto>> addToCart(@RequestHeader(value = "Authorization")String authHeader,@Valid @RequestBody CartRequestDto cartRequestDto)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if(userDetails != null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return cartService.addToCart(userDetails.getUsername(),cartRequestDto);
        }
        return new ResponseEntity<>(cartMapper.noAuthority(), HttpStatus.UNAUTHORIZED);
    }

    @DeleteMapping("/removeFromCart/{cartId}")
    public ResponseEntity<ResponseStructure<CartResponseDto>> removeFromCart(@RequestHeader(value = "Authorization")String authHeader,@PathVariable Long cartId)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if(userDetails != null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return cartService.removeFromCart(userDetails.getUsername(),cartId);
        }
        return new ResponseEntity<>(cartMapper.noAuthority(), HttpStatus.UNAUTHORIZED);
    }

    @GetMapping("/getCart")
    public ResponseEntity<ResponseStructure<List<CartResponseDto>>> getCartItems(@RequestHeader(value = "Authorization")String authHeader)
    {
        UserDetails userDetails = userMapper.validateUserToken(authHeader);
        if(userDetails != null && userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
        {
            return cartService.getCartItems(userDetails.getUsername());
        }
        return new ResponseEntity<>(cartMapper.noAuthority("No Authority"), HttpStatus.UNAUTHORIZED);
    }
}
