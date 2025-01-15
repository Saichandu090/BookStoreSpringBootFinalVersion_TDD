package com.example.demo.service;

import com.example.demo.entity.Book;
import com.example.demo.entity.User;
import com.example.demo.entity.WishList;
import com.example.demo.exception.BookNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WishListRepository;
import com.example.demo.requestdto.WishListRequestDto;
import com.example.demo.responsedto.BookResponseDto;
import com.example.demo.responsedto.WishListResponseDto;
import com.example.demo.serviceimpl.WishListServiceImpl;
import com.example.demo.util.ResponseStructure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.parameters.P;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WishListServiceTest
{
    @Mock
    private WishListRepository wishListRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private WishListServiceImpl wishListService;

    private User user;
    private Book book;
    private WishList wishList;

    private WishListRequestDto wishListRequestDto;

    @BeforeEach
    public void init()
    {
        user=User.builder()
                .email("test@gmail.com")
                .userId(100L)
                .password("test@90909")
                .dob(LocalDate.of(1999,8,12))
                .firstName("Mock")
                .lastName("Testing")
                .role("USER")
                .registeredDate(LocalDate.now()).build();

        book=Book.builder()
                .bookId(1L)
                .bookLogo("URL")
                .bookName("ATOM")
                .bookAuthor("Author")
                .bookDescription("Descript")
                .bookPrice(123.12)
                .cartBookQuantity(0)
                .build();

        wishListRequestDto=WishListRequestDto.builder().bookId(book.getBookId()).build();

        wishList=WishList.builder().id(1L).bookId(book.getBookId()).userId(user.getUserId()).build();
    }

    @Test
    public void wishListService_AddToWishList_MustReturnCreatedStatusCode()
    {
        when(wishListRepository.save(any(WishList.class))).thenReturn(wishList);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));

        ResponseEntity<ResponseStructure<WishListResponseDto>> response=wishListService.addToWishList(user.getEmail(),wishListRequestDto);

        assertEquals(HttpStatus.CREATED,response.getStatusCode());
        assertEquals(201,response.getBody().getStatus());
        assertEquals(book.getBookId(),response.getBody().getData().getBookId());

        verify(wishListRepository,times(1)).save(any(WishList.class));
        verify(userRepository,times(1)).findByEmail(anyString());
        verify(bookRepository,times(1)).findById(anyLong());
    }

    @Test
    public void wishListService_AddToWishList_TestWhenBookIdIsWrong()
    {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class,()->wishListService.addToWishList(user.getEmail(),wishListRequestDto));

        verify(bookRepository,times(1)).findById(anyLong());
        verify(userRepository,times(1)).findByEmail(anyString());
    }

    @Test
    public void wishListService_AddToWishList_TestWhenUserEmailIsWrong()
    {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class,()->wishListService.addToWishList(user.getEmail(),wishListRequestDto));
        verify(userRepository,times(1)).findByEmail(anyString());
    }

    @Test
    public void wishListService_RemoveFromWishList_MustReturnOkStatusCode()
    {
        List<WishList> wishLists=new ArrayList<>();
        wishLists.add(wishList);

        User user1=User.builder()
                        .wishList(wishLists).build();
        when(wishListRepository.saveAll(anyList())).thenReturn(List.of());
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user1));
        when(bookRepository.findById(anyLong())).thenReturn(Optional.of(book));

        ResponseEntity<ResponseStructure<WishListResponseDto>> response=wishListService.removeFromWishList(user.getEmail(),book.getBookId());

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(200,response.getBody().getStatus());

        verify(wishListRepository,times(1)).saveAll(anyList());
        verify(userRepository,times(1)).findByEmail(anyString());
        verify(bookRepository,times(1)).findById(anyLong());
    }

    @Test
    public void wishListService_RemoveFromWishList_TestWhenBookIdIsWrong()
    {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(bookRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class,()->wishListService.removeFromWishList(user.getEmail(),book.getBookId()));

        verify(bookRepository,times(1)).findById(anyLong());
        verify(userRepository,times(1)).findByEmail(anyString());
    }

    @Test
    public void wishListService_RemoveFromWishList_TestWhenUserEmailIsWrong()
    {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class,()->wishListService.removeFromWishList(user.getEmail(),book.getBookId()));
        verify(userRepository,times(1)).findByEmail(anyString());
    }

}