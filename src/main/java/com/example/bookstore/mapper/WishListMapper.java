package com.example.bookstore.mapper;

import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.WishList;
import com.example.bookstore.responsedto.WishListResponse;
import com.example.bookstore.util.ResponseStructure;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class WishListMapper
{
    public ResponseEntity<ResponseStructure<WishListResponse>> headerError()
    {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseStructure<WishListResponse>()
                .setData(null)
                .setMessage("Token Error")
                .setStatus(HttpStatus.UNAUTHORIZED.value()));
    }

    public ResponseEntity<ResponseStructure<List<WishListResponse>>> noAuthority()
    {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseStructure<List<WishListResponse>>()
                .setStatus(HttpStatus.UNAUTHORIZED.value())
                .setData(null)
                .setMessage("Token Error"));
    }

    public WishListResponse mapWishListToResponse(WishList saved)
    {
        return WishListResponse.builder()
                .wishListId(saved.getWishListId())
                .bookId(saved.getBookId()).build();
    }

    public WishList mapToWishList(Long bookId, Long userId)
    {
        return WishList.builder()
                .bookId(bookId)
                .userId(userId).build();
    }

    public ResponseEntity<ResponseStructure<WishListResponse>> mapWishListSuccessResponseCREATED(WishList saved, Book book)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseStructure<WishListResponse>()
                .setStatus(HttpStatus.CREATED.value())
                .setMessage("Book "+book.getBookName()+" has added to wishlist successfully")
                .setData(mapWishListToResponse(saved)));
    }

    public ResponseEntity<ResponseStructure<WishListResponse>> mapToSuccessResponseOk(String bookName)
    {
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<WishListResponse>()
                .setStatus(HttpStatus.OK.value())
                .setMessage("Book "+bookName+" has successfully removed from wishlist successfully")
                .setData(null));
    }

    public ResponseEntity<ResponseStructure<List<WishListResponse>>> mapToNoContentInWishList()
    {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ResponseStructure<List<WishListResponse>>()
                .setStatus(HttpStatus.NO_CONTENT.value())
                .setMessage("WishList is Empty")
                .setData(null));
    }

    public ResponseEntity<ResponseStructure<List<WishListResponse>>> mapToSuccessGetWishList(List<WishList> userWishList)
    {
        List<WishListResponse> wishListResponse =userWishList.stream().map(wishList -> new WishListResponse(wishList.getWishListId(), wishList.getBookId())).toList();
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<List<WishListResponse>>()
                .setMessage("Wishlist fetched successfully")
                .setData(wishListResponse)
                .setStatus(HttpStatus.OK.value()));
    }

    public ResponseEntity<ResponseStructure<Boolean>> noAuthorityForUser()
    {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseStructure<Boolean>()
                .setStatus(HttpStatus.UNAUTHORIZED.value())
                .setData(null)
                .setMessage("Token Error"));
    }


    public ResponseEntity<ResponseStructure<Boolean>> mapToNotInWishList()
    {
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<Boolean>()
                .setMessage("Book is not in wishlist")
                .setData(false)
                .setStatus(HttpStatus.OK.value()));
    }

    public ResponseEntity<ResponseStructure<Boolean>> mapToIsInWishList()
    {
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<Boolean>()
                .setMessage("Book is in wishlist")
                .setData(true)
                .setStatus(HttpStatus.OK.value()));
    }
}
