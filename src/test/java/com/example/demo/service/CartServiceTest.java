package com.example.demo.service;

import com.example.demo.entity.Book;
import com.example.demo.entity.Cart;
import com.example.demo.entity.User;
import com.example.demo.exception.BookNotFoundException;
import com.example.demo.exception.CartNotFoundException;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.requestdto.CartRequestDto;
import com.example.demo.responsedto.CartResponseDto;
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

    private CartRequestDto cartRequestDto;
    private Cart cart;
    private User user;
    private Book book;

    @BeforeEach
    void init()
    {
        book=Book.builder().bookId(1L).bookName("Atom").bookQuantity(10).build();

        cartRequestDto=CartRequestDto.builder().bookId(1L).build();
        cart=Cart.builder()
                .cartId(1L)
                .cartQuantity(1)
                .bookId(1L)
                .userId(1L).build();

        List<Cart> carts=new ArrayList<>();
        carts.add(cart);

        user=User.builder()
                .firstName("Sai")
                .lastName("Chandu")
                .dob(LocalDate.of(2002,8,24))
                .email("sai@gmail.com")
                .password("saichandu")
                .carts(carts)
                .role("USER").build();
    }


    @Test
    void cartService_AddToCart_ValidTest()
    {
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        ResponseEntity<ResponseStructure<CartResponseDto>> response=cartService.addToCart(user.getEmail(),cartRequestDto);

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());
        assertEquals(2,response.getBody().getData().getCartQuantity());
        assertEquals(1,response.getBody().getData().getBookId());
    }


    @Test
    void cartService_AddToCart_IfBookIsOutOfStock()
    {
        book=Book.builder().bookId(1L).bookName("Atom").bookQuantity(0).build();
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        ResponseEntity<ResponseStructure<CartResponseDto>> response=cartService.addToCart(user.getEmail(),cartRequestDto);

        assertEquals(HttpStatus.NO_CONTENT,response.getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT.value(),response.getBody().getStatus());
        assertEquals("Book "+book.getBookName()+" is out of stock!!",response.getBody().getMessage());
    }

    @Test
    void cartService_AddToCart_IfBookNotFound()
    {
        when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        assertThrows(BookNotFoundException.class,()->cartService.addToCart(user.getEmail(),cartRequestDto));

        verify(bookRepository,times(1)).findById(anyLong());
    }


    @Test
    void cartService_RemoveFromCart_ValidTest()
    {
        when(cartRepository.findById(anyLong())).thenReturn(Optional.of(cart));
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(bookRepository.findByIdForUpdate(anyLong())).thenReturn(Optional.of(book));

        ResponseEntity<ResponseStructure<CartResponseDto>> response=cartService.removeFromCart(user.getEmail(),cart.getCartId());

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals("Book "+book.getBookName()+" has removed from the cart",response.getBody().getMessage());
        verify(cartRepository,times(1)).delete(any(Cart.class));
        verify(cartRepository,times(1)).findById(anyLong());
        verify(userRepository,times(1)).findByEmail(anyString());
    }


    @Test
    void cartService_RemoveFromCart_IfCartIdNotFound()
    {
        when(cartRepository.findById(anyLong())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        assertThrows(CartNotFoundException.class,()->cartService.removeFromCart(user.getEmail(),cart.getCartId()));

        verify(cartRepository,times(1)).findById(anyLong());
    }


    @Test
    void cartService_GetCart_ValidTest()
    {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        ResponseEntity<ResponseStructure<List<CartResponseDto>>> response=cartService.getCartItems(user.getEmail());

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(1,response.getBody().getData().getFirst().getCartId());
        assertEquals(1,response.getBody().getData().getFirst().getBookId());
        assertEquals(1,response.getBody().getData().getFirst().getCartQuantity());

        verify(userRepository,times(1)).findByEmail(anyString());
    }

    @Test
    void cartService_GetCart_IfCartIsEmpty()
    {
        User user1=User.builder().email("test@gmail.com").carts(new ArrayList<>()).build();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user1));

        ResponseEntity<ResponseStructure<List<CartResponseDto>>> response=cartService.getCartItems(user.getEmail());

        assertEquals(HttpStatus.NO_CONTENT,response.getStatusCode());
        assertEquals("Cart is Empty",response.getBody().getMessage());

        verify(userRepository,times(1)).findByEmail(anyString());
    }
}