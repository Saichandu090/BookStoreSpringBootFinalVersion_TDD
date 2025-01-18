package com.example.demo.mapper;

import com.example.demo.entity.Book;
import com.example.demo.entity.WishList;
import com.example.demo.responsedto.WishListResponseDto;
import com.example.demo.util.ResponseStructure;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class WishListMapper
{
    public ResponseStructure<WishListResponseDto> headerError()
    {
        return new ResponseStructure<WishListResponseDto>()
                .setData(null)
                .setMessage("Token Error")
                .setStatus(HttpStatus.UNAUTHORIZED.value());
    }

    public ResponseStructure<List<WishListResponseDto>> noAuthority()
    {
        return new ResponseStructure<List<WishListResponseDto>>()
                .setStatus(HttpStatus.UNAUTHORIZED.value())
                .setData(null)
                .setMessage("Token Error");
    }

    public WishListResponseDto mapWishListToResponse(WishList saved)
    {
        return WishListResponseDto.builder()
                .wishListId(saved.getWishListId())
                .bookId(saved.getBookId()).build();
    }

    public WishList mapToWishList(Long bookId, Long userId)
    {
        return WishList.builder()
                .bookId(bookId)
                .userId(userId).build();
    }

    public ResponseEntity<ResponseStructure<WishListResponseDto>> mapWishListSuccessResponseCREATED(WishList saved)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseStructure<WishListResponseDto>()
                .setStatus(HttpStatus.CREATED.value())
                .setMessage("Book added to wishlist successfully")
                .setData(mapWishListToResponse(saved)));
    }

    public ResponseEntity<ResponseStructure<WishListResponseDto>> mapToSuccessResponseOk(String bookName)
    {
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<WishListResponseDto>()
                .setStatus(HttpStatus.OK.value())
                .setMessage("Book "+bookName+" has successfully removed from wishlist successfully")
                .setData(null));
    }

    public ResponseEntity<ResponseStructure<List<WishListResponseDto>>> mapToNoContentInWishList()
    {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ResponseStructure<List<WishListResponseDto>>()
                .setStatus(HttpStatus.NO_CONTENT.value())
                .setMessage("WishList is Empty")
                .setData(null));
    }

    public ResponseEntity<ResponseStructure<List<WishListResponseDto>>> mapToSuccessGetWishList(List<WishList> userWishList)
    {
        List<WishListResponseDto> wishListResponseDto=userWishList.stream().map(wishList -> new WishListResponseDto(wishList.getWishListId(), wishList.getBookId())).toList();
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<List<WishListResponseDto>>()
                .setMessage("Wishlist fetched successfully")
                .setData(wishListResponseDto)
                .setStatus(HttpStatus.OK.value()));
    }
}
