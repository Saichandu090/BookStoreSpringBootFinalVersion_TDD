package com.example.bookstore.service;

import com.example.bookstore.requestdto.WishListRequest;
import com.example.bookstore.responsedto.WishListResponse;
import com.example.bookstore.util.ResponseStructure;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface WishListService
{
    ResponseEntity<ResponseStructure<WishListResponse>> addToWishList(String email, WishListRequest wishListRequest);

    ResponseEntity<ResponseStructure<List<WishListResponse>>> getWishList(String username);

    ResponseEntity<ResponseStructure<Boolean>> isInWishList(String username, Long bookId);
}
