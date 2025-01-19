package com.example.demo.mapper;

import com.example.demo.entity.Book;
import com.example.demo.entity.Cart;
import com.example.demo.responsedto.CartResponseDto;
import com.example.demo.util.ResponseStructure;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class CartMapper
{
    public ResponseEntity<ResponseStructure<CartResponseDto>> noAuthority()
    {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseStructure<CartResponseDto>()
                .setMessage("No Authority")
                .setData(null)
                .setStatus(HttpStatus.UNAUTHORIZED.value()));
    }

    public ResponseEntity<ResponseStructure<List<CartResponseDto>>> noAuthority(String message)
    {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseStructure<List<CartResponseDto>>()
                .setMessage(message)
                .setData(null)
                .setStatus(HttpStatus.UNAUTHORIZED.value()));
    }

    public ResponseEntity<ResponseStructure<CartResponseDto>> mapToSuccessRemoveFromCart(String bookName)
    {
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<CartResponseDto>()
                .setStatus(HttpStatus.OK.value())
                .setMessage("Book "+bookName+" has removed from the cart")
                .setData(null));
    }

    public ResponseEntity<ResponseStructure<CartResponseDto>> mapToBookOutOfStock(Book book)
    {
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ResponseStructure<CartResponseDto>()
                .setMessage("Book "+book.getBookName()+" is out of stock!!")
                .setStatus(HttpStatus.NO_CONTENT.value())
                .setData(null));
    }

    public ResponseEntity<ResponseStructure<CartResponseDto>> mapToSuccessAddToCart(Cart cart)
    {
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<CartResponseDto>()
                .setData(new CartResponseDto(cart.getCartId(), cart.getBookId(),cart.getCartQuantity()))
                .setMessage("Book added to cart successfully")
                .setStatus(HttpStatus.OK.value()));
    }

    public ResponseEntity<ResponseStructure<List<CartResponseDto>>> mapToCartIsEmpty()
    {
        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<ResponseStructure<List<CartResponseDto>>> mapToSuccessGetCart(List<CartResponseDto> cartResponseDto)
    {
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<List<CartResponseDto>>()
                .setMessage("User cart fetched successfully")
                .setStatus(HttpStatus.OK.value())
                .setData(cartResponseDto));
    }

    public ResponseEntity<ResponseStructure<CartResponseDto>> mapToSuccessClearCart()
    {
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<CartResponseDto>()
                .setStatus(HttpStatus.OK.value())
                .setMessage("Cart cleared successfully")
                .setData(null));
    }

    public ResponseEntity<ResponseStructure<CartResponseDto>> mapToNoContentCartIsEmpty()
    {
        return ResponseEntity.noContent().build();
    }

    public CartResponseDto mapToCartResponse(Cart cart)
    {
        return CartResponseDto.builder()
                .bookId(cart.getBookId())
                .cartId(cart.getCartId())
                .cartQuantity(cart.getCartQuantity()).build();
    }
}
