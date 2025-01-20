package com.example.bookstore.mapper;

import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.Cart;
import com.example.bookstore.responsedto.CartResponse;
import com.example.bookstore.util.ResponseStructure;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class CartMapper
{
    public ResponseEntity<ResponseStructure<CartResponse>> noAuthority()
    {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseStructure<CartResponse>()
                .setMessage("No Authority")
                .setData(null)
                .setStatus(HttpStatus.UNAUTHORIZED.value()));
    }

    public ResponseEntity<ResponseStructure<List<CartResponse>>> noAuthority(String message)
    {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseStructure<List<CartResponse>>()
                .setMessage(message)
                .setData(null)
                .setStatus(HttpStatus.UNAUTHORIZED.value()));
    }

    public ResponseEntity<ResponseStructure<CartResponse>> mapToSuccessRemoveFromCart(String bookName)
    {
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<CartResponse>()
                .setStatus(HttpStatus.OK.value())
                .setMessage("Book "+bookName+" has removed from the cart")
                .setData(null));
    }

    public ResponseEntity<ResponseStructure<CartResponse>> mapToBookOutOfStock(Book book)
    {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseStructure<CartResponse>()
                .setMessage("Book "+book.getBookName()+" is out of stock!!")
                .setStatus(HttpStatus.CONFLICT.value())
                .setData(null));
    }

    public ResponseEntity<ResponseStructure<CartResponse>> mapToSuccessAddToCart(Cart cart)
    {
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<CartResponse>()
                .setData(new CartResponse(cart.getCartId(), cart.getBookId(),cart.getCartQuantity()))
                .setMessage("Book added to cart successfully")
                .setStatus(HttpStatus.OK.value()));
    }

    public ResponseEntity<ResponseStructure<List<CartResponse>>> mapToCartIsEmpty()
    {
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<ResponseStructure<List<CartResponse>>> mapToSuccessGetCart(List<CartResponse> cartResponse)
    {
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<List<CartResponse>>()
                .setMessage("User cart fetched successfully")
                .setStatus(HttpStatus.OK.value())
                .setData(cartResponse));
    }

    public ResponseEntity<ResponseStructure<CartResponse>> mapToSuccessClearCart()
    {
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<CartResponse>()
                .setStatus(HttpStatus.OK.value())
                .setMessage("Cart cleared successfully")
                .setData(null));
    }

    public ResponseEntity<ResponseStructure<CartResponse>> mapToNoContentCartIsEmpty()
    {
        return ResponseEntity.noContent().build();
    }

    public CartResponse mapToCartResponse(Cart cart)
    {
        return CartResponse.builder()
                .bookId(cart.getBookId())
                .cartId(cart.getCartId())
                .cartQuantity(cart.getCartQuantity()).build();
    }
}
