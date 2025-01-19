package com.example.demo.service;

import com.example.demo.entity.Book;
import com.example.demo.entity.Cart;
import com.example.demo.entity.User;
import com.example.demo.exception.BookNotFoundException;
import com.example.demo.exception.CartNotFoundException;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.requestdto.CartRequest;
import com.example.demo.responsedto.CartResponse;
import com.example.demo.serviceimpl.CartServiceImpl;
import com.example.demo.util.ResponseStructure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest
{
    @Mock
    private CartRepository cartRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    private CartRequest cartRequest;
    private Cart cart;
    private User user;
    private Book book;

    @BeforeEach
    void init()
    {
        book=Book.builder().bookId(1L).bookName("Atom").bookQuantity(10).build();

        cartRequest = CartRequest.builder().bookId(1L).build();
        cart=Cart.builder()
                .cartId(1L)
                .cartQuantity(1)
                .bookId(1L)
                .userId(1L).build();

        List<Cart> carts=new ArrayList<>();
        carts.add(cart);

        user=User.builder()
                .userId(1L)
                .firstName("Sai")
                .lastName("Chandu")
                .dob(LocalDate.of(2002,8,24))
                .email("sai@gmail.com")
                .password("saichandu")
                .carts(carts)
                .role("USER").build();
    }


    @Test
    void cartServiceAddToCartValidTest()
    {
        when(bookRepository.findByIdForUpdate(anyLong())).thenReturn(Optional.of(book));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        ResponseEntity<ResponseStructure<CartResponse>> response=cartService.addToCart(user.getEmail(), cartRequest);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());
        assertEquals(2,response.getBody().getData().getCartQuantity());
        assertEquals(1,response.getBody().getData().getBookId());
    }


    @Test
    void cartServiceAddToCartIfBookIsOutOfStock()
    {
        book=Book.builder().bookId(1L).bookName("Atom").bookQuantity(0).build();
        when(bookRepository.findByIdForUpdate(anyLong())).thenReturn(Optional.of(book));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        ResponseEntity<ResponseStructure<CartResponse>> response=cartService.addToCart(user.getEmail(), cartRequest);

        assertEquals(HttpStatus.NO_CONTENT,response.getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT.value(),response.getBody().getStatus());
        assertEquals("Book "+book.getBookName()+" is out of stock!!",response.getBody().getMessage());
    }

    @Test
    void cartServiceAddToCartIfBookNotFound()
    {
        when(bookRepository.findByIdForUpdate(anyLong())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        assertThrows(BookNotFoundException.class,()->cartService.addToCart(user.getEmail(), cartRequest));

        verify(bookRepository,times(1)).findByIdForUpdate(anyLong());
    }


    @Test
    void cartServiceRemoveFromCartValidTest()
    {
        when(cartRepository.findByCartIdAndUserId(anyLong(),anyLong())).thenReturn(Optional.of(cart));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(bookRepository.findByIdForUpdate(anyLong())).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        ResponseEntity<ResponseStructure<CartResponse>> response=cartService.removeFromCart(user.getEmail(),cart.getCartId());

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals("Book "+book.getBookName()+" has removed from the cart",response.getBody().getMessage());
        verify(cartRepository,times(1)).delete(any(Cart.class));
        verify(cartRepository,times(1)).findByCartIdAndUserId(anyLong(),anyLong());
        verify(userRepository,times(1)).findByEmail(anyString());
    }


    @Test
    void cartServiceRemoveFromCartIfCartIdNotFound()
    {
        when(cartRepository.findByCartIdAndUserId(anyLong(),anyLong())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        assertThrows(CartNotFoundException.class,()->cartService.removeFromCart(user.getEmail(),cart.getCartId()));

        verify(cartRepository,times(1)).findByCartIdAndUserId(anyLong(),anyLong());
    }


    @Test
    void cartServiceGetCartValidTest()
    {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        ResponseEntity<ResponseStructure<List<CartResponse>>> response=cartService.getCartItems(user.getEmail());

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(1,response.getBody().getData().getFirst().getCartId());
        assertEquals(1,response.getBody().getData().getFirst().getBookId());
        assertEquals(1,response.getBody().getData().getFirst().getCartQuantity());

        verify(userRepository,times(1)).findByEmail(anyString());
    }

    @Test
    void cartServiceGetCartIfCartIsEmpty()
    {
        User user1=User.builder().email("test@gmail.com").carts(new ArrayList<>()).build();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user1));

        ResponseEntity<ResponseStructure<List<CartResponse>>> response=cartService.getCartItems(user.getEmail());
        assertEquals(HttpStatus.NO_CONTENT,response.getStatusCode());

        verify(userRepository,times(1)).findByEmail(anyString());
    }


    @Test
    void cartServiceClearCartValidTest()
    {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(bookRepository.findByIdForUpdate(anyLong())).thenReturn(Optional.of(book));

        ResponseEntity<ResponseStructure<CartResponse>> response=cartService.clearCart(user.getEmail());

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals("Cart cleared successfully",response.getBody().getMessage());
        verify(cartRepository,times(1)).delete(any(Cart.class));
        verify(userRepository,times(1)).findByEmail(anyString());
    }

    @Test
    void cartServiceClearCartIfCartIsEmpty()
    {
        User user1=User.builder().email("test@gmail.com").carts(new ArrayList<>()).build();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user1));

        ResponseEntity<ResponseStructure<CartResponse>> response=cartService.clearCart(user.getEmail());

        assertEquals(HttpStatus.NO_CONTENT,response.getStatusCode());

        verify(userRepository,times(1)).findByEmail(anyString());
    }
}