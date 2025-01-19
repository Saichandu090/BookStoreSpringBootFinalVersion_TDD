package com.example.demo.service;

import com.example.demo.requestdto.WishListRequest;
import com.example.demo.responsedto.WishListResponse;
import com.example.demo.util.ResponseStructure;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface WishListService
{
    ResponseEntity<ResponseStructure<WishListResponse>> addToWishList(String email, WishListRequest wishListRequest);

    ResponseEntity<ResponseStructure<List<WishListResponse>>> getWishList(String username);
}
