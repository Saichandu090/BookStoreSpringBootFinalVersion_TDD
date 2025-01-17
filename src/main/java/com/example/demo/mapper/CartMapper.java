package com.example.demo.mapper;

import com.example.demo.entity.Book;
import com.example.demo.entity.Cart;
import com.example.demo.responsedto.CartResponseDto;
import com.example.demo.util.ResponseStructure;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class CartMapper
{
    public ResponseStructure<CartResponseDto> noAuthority()
    {
        return new ResponseStructure<CartResponseDto>()
                .setMessage("No Authority")
                .setData(null)
                .setStatus(HttpStatus.UNAUTHORIZED.value());
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
}
