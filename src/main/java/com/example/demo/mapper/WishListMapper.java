package com.example.demo.mapper;

import com.example.demo.entity.Book;
import com.example.demo.entity.WishList;
import com.example.demo.responsedto.WishListResponseDto;
import com.example.demo.util.ResponseStructure;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class WishListMapper
{
    public ResponseStructure<WishListResponseDto> headerError()
    {
        return new ResponseStructure<WishListResponseDto>()
                .setData(null)
                .setMessage("Token Error")
                .setStatus(HttpStatus.UNAUTHORIZED.value());
    }

    public ResponseStructure<WishListResponseDto> noAuthority()
    {
        return new ResponseStructure<WishListResponseDto>()
                .setStatus(HttpStatus.FORBIDDEN.value())
                .setData(null)
                .setMessage("No Authority");
    }

    public WishListResponseDto mapWishListToResponse(WishList saved)
    {
        return WishListResponseDto.builder()
                .wishListId(saved.getId())
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
}
