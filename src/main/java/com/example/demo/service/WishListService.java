package com.example.demo.service;

import com.example.demo.requestdto.WishListRequestDto;
import com.example.demo.responsedto.WishListResponseDto;
import com.example.demo.util.ResponseStructure;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface WishListService
{
    ResponseEntity<ResponseStructure<WishListResponseDto>> addToWishList(String email,WishListRequestDto wishListRequestDto);

    ResponseEntity<ResponseStructure<List<WishListResponseDto>>> getWishList(String username);
}
