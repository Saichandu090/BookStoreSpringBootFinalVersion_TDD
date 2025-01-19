package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.mapper.BookMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.requestdto.BookRequest;
import com.example.demo.responsedto.BookResponse;
import com.example.demo.service.BookService;
import com.example.demo.util.ResponseStructure;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.CoreMatchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookController.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc(addFilters = false)
public class BookControllerTests
{
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private BookService bookService;

    @MockitoBean
    private UserMapper userMapper;

    @MockitoBean
    private BookMapper bookMapper;

    private User user;
    private User admin;
    private UserDetails adminDetails;
    private UserDetails userDetails;
    private BookRequest bookRequest;
    private BookResponse bookResponse;

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

        admin=User.builder()
                .email("sai@gmail.com")
                .userId(1L)
                .password("saichandu090")
                .dob(LocalDate.of(2002,8,24))
                .firstName("Sai")
                .lastName("Chandu")
                .role("ADMIN")
                .registeredDate(LocalDate.now()).build();

        adminDetails=new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return Collections.singleton(new SimpleGrantedAuthority(admin.getRole()));
            }

            @Override
            public String getPassword() {
                return admin.getPassword();
            }

            @Override
            public String getUsername() {
                return admin.getEmail();
            }
        };

        userDetails=new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities()
            {
                return Collections.singleton(new SimpleGrantedAuthority(user.getRole()));
            }

            @Override
            public String getPassword()
            {
                return user.getPassword();
            }

            @Override
            public String getUsername() {
                return user.getEmail();
            }
        };

        bookRequest = BookRequest.builder()
                .bookId((long)789654123)
                .bookName("ABCD")
                .bookPrice(789.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        bookResponse = BookResponse.builder()
                .bookId(bookRequest.getBookId())
                .bookLogo(bookRequest.getBookLogo())
                .bookName(bookRequest.getBookName())
                .bookAuthor(bookRequest.getBookAuthor())
                .bookDescription(bookRequest.getBookDescription())
                .bookPrice(bookRequest.getBookPrice())
                .build();
    }

    @Test
    public void bookControllerAddBookTestMustReturnCreatedStatusCode() throws Exception
    {
        String token="Bearer-token";
        ResponseStructure<BookResponse> responseStructure=new ResponseStructure<>(HttpStatus.CREATED.value(),"Book added successfully", bookResponse);
        given(bookService.addBook(ArgumentMatchers.any(BookRequest.class))).willReturn(new ResponseEntity<>(responseStructure,HttpStatus.CREATED));
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(adminDetails);

        mockMvc.perform(post("/book/addBook")
                        .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token)
                .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",CoreMatchers.is(HttpStatus.CREATED.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.bookName",CoreMatchers.is(bookResponse.getBookName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.bookId",CoreMatchers.is(bookResponse.getBookId().intValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",CoreMatchers.is(responseStructure.getMessage())))
                .andDo(MockMvcResultHandlers.print());
    }


    @Test
    public void bookControllerAddBookTestMustReturnFailCauseOfNotValidBody() throws Exception
    {
        BookRequest requestDto= BookRequest.builder().build();
        String token="Bearer-token";
        ResponseStructure<BookResponse> responseStructure=new ResponseStructure<>(HttpStatus.CREATED.value(),"Book added successfully", bookResponse);
        given(bookService.addBook(ArgumentMatchers.any(BookRequest.class))).willReturn(new ResponseEntity<>(responseStructure,HttpStatus.CREATED));
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(adminDetails);

        mockMvc.perform(post("/book/addBook")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MethodArgumentNotValidException.class, result.getResolvedException()))
                .andDo(MockMvcResultHandlers.print());
    }


    @Test
    public void bookControllerAddBookTestMustReturnUnauthorizedStatusCode() throws Exception
    {
        String token="Bearer-token";
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(userDetails);

        mockMvc.perform(post("/book/addBook")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",CoreMatchers.is(HttpStatus.UNAUTHORIZED.value())))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void bookControllerAddBookTestMustReturnBadRequestForMissingHeaderStatusCode() throws Exception
    {
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(adminDetails);

        mockMvc.perform(post("/book/addBook")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MissingRequestHeaderException.class,result.getResolvedException()))
                .andDo(MockMvcResultHandlers.print());
    }



    @Test
    public void bookControllerGetBookByNameMustReturnOKStatusCode() throws Exception
    {
        String token="Bearer-token";
        ResponseStructure<BookResponse> responseStructure=new ResponseStructure<>(HttpStatus.OK.value(),"Book fetched successfully", bookResponse);
        given(bookService.getBookByName(ArgumentMatchers.anyString())).willReturn(new ResponseEntity<>(responseStructure,HttpStatus.OK));
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(userDetails);

        mockMvc.perform(get("/book/getBookByName/{bookName}", bookRequest.getBookName())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",CoreMatchers.is(HttpStatus.OK.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.bookName",CoreMatchers.is(bookResponse.getBookName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.bookId",CoreMatchers.is(bookResponse.getBookId().intValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",CoreMatchers.is(responseStructure.getMessage())))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void bookControllerGetBookByNameMustReturnBadRequestWithoutHeader() throws Exception
    {
        mockMvc.perform(get("/book/getBookByName/{bookName}", bookRequest.getBookName())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MissingRequestHeaderException.class,result.getResolvedException()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void bookControllerGetBookByNameMissingPathVariable() throws Exception
    {
        String token="Bearer token";
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(userDetails);

        mockMvc.perform(get("/book/getBookByName")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("Authorization",token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(NoResourceFoundException.class,result.getResolvedException()))
                .andDo(MockMvcResultHandlers.print());
    }



    @Test
    public void bookControllerGetBookByIdMustReturnOKStatusCode() throws Exception
    {
        String token="Bearer-token";
        ResponseStructure<BookResponse> responseStructure=new ResponseStructure<>(HttpStatus.OK.value(),"Book fetched successfully", bookResponse);
        given(bookService.getBookById(ArgumentMatchers.anyLong())).willReturn(new ResponseEntity<>(responseStructure,HttpStatus.OK));
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(adminDetails);

        mockMvc.perform(get("/book/getBookById/{bookId}", bookRequest.getBookId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("Authorization",token))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",CoreMatchers.is(HttpStatus.OK.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.bookName",CoreMatchers.is(bookResponse.getBookName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.bookId",CoreMatchers.is(bookResponse.getBookId().intValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",CoreMatchers.is(responseStructure.getMessage())))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void bookControllerGetBookByIdTestWithoutHeader() throws Exception
    {
        mockMvc.perform(get("/book/getBookById/{bookId}", bookRequest.getBookId())
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MissingRequestHeaderException.class,result.getResolvedException()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void bookControllerGetBookByIdTestWhenMissingPathVariable() throws Exception
    {
        String token="Bearer-token";
        ResponseStructure<BookResponse> responseStructure=new ResponseStructure<>(HttpStatus.OK.value(),"Book fetched successfully", bookResponse);
        given(bookService.getBookById(ArgumentMatchers.anyLong())).willReturn(new ResponseEntity<>(responseStructure,HttpStatus.OK));
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(adminDetails);

        mockMvc.perform(get("/book/getBookById")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("Authorization",token))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(NoResourceFoundException.class,result.getResolvedException()))
                .andDo(MockMvcResultHandlers.print());
    }



    @Test
    public void bookControllerGetAllBooksMustReturnOKStatusCode() throws Exception
    {
        String token="Bearer-token";
        ResponseStructure<List<BookResponse>> responseStructure=new ResponseStructure<>(HttpStatus.OK.value(),"Book fetched successfully",List.of(bookResponse));
        given(bookService.getAllBooks()).willReturn(new ResponseEntity<>(responseStructure,HttpStatus.OK));
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(adminDetails);

        mockMvc.perform(get("/book/getBooks")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("Authorization",token))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",CoreMatchers.is(HttpStatus.OK.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.size()",CoreMatchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].bookName",CoreMatchers.is(bookResponse.getBookName())))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void bookControllerGetAllBooksTestWithoutHeader() throws Exception
    {
        mockMvc.perform(get("/book/getBooks")
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MissingRequestHeaderException.class,result.getResolvedException()))
                .andDo(MockMvcResultHandlers.print());
    }



    @Test
    public void bookControllerUpdateBookMustReturnOKStatusCode() throws Exception
    {
        String token="Bearer-token";
        ResponseStructure<BookResponse> responseStructure=new ResponseStructure<>(HttpStatus.OK.value(),"Book Updated successfully", bookResponse);
        given(bookService.updateBook(ArgumentMatchers.anyLong(),ArgumentMatchers.any(BookRequest.class))).willReturn(new ResponseEntity<>(responseStructure,HttpStatus.OK));
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(adminDetails);

        mockMvc.perform(put("/book/updateBook/{bookId}", bookRequest.getBookId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",CoreMatchers.is(HttpStatus.OK.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.bookName",CoreMatchers.is(bookResponse.getBookName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.bookId",CoreMatchers.is(bookResponse.getBookId().intValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",CoreMatchers.is(responseStructure.getMessage())))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void bookControllerUpdateBookTestWithoutHeader() throws Exception
    {
        mockMvc.perform(put("/book/updateBook/{bookId}", bookRequest.getBookId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MissingRequestHeaderException.class,result.getResolvedException()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void bookControllerUpdateBookTestForUserAccess() throws Exception
    {
        String token="Bearer-token";
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(userDetails);

        mockMvc.perform(put("/book/updateBook/{bookId}", bookRequest.getBookId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",CoreMatchers.is(HttpStatus.UNAUTHORIZED.value())))
                .andDo(MockMvcResultHandlers.print());
    }


    @Test
    public void bookControllerUpdateBookTestForInvalidRequestBody() throws Exception
    {
        BookRequest bookRequest =new BookRequest();
        String token="Bearer-token";
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(adminDetails);

        mockMvc.perform(put("/book/updateBook/{bookId}", this.bookRequest.getBookId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token)
                        .content(objectMapper.writeValueAsString(bookRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MethodArgumentNotValidException.class,result.getResolvedException()))
                .andDo(MockMvcResultHandlers.print());
    }



    @Test
    public void bookControllerDeleteBookMustReturnOKStatusCode() throws Exception
    {
        String token="Bearer-token";
        ResponseStructure<String> responseStructure=new ResponseStructure<>(HttpStatus.OK.value(),"Book deleted successfully","Success");
        given(bookService.deleteBook(ArgumentMatchers.anyLong())).willReturn(new ResponseEntity<>(responseStructure,HttpStatus.OK));
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(adminDetails);

        mockMvc.perform(delete("/book/deleteBook/{bookId}", bookRequest.getBookId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",CoreMatchers.is(HttpStatus.OK.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data",CoreMatchers.is(responseStructure.getData())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",CoreMatchers.is(responseStructure.getMessage())))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void bookControllerDeleteBookTestWhenUserTriesToAccess() throws Exception
    {
        String token="Bearer-token";
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(userDetails);

        mockMvc.perform(delete("/book/deleteBook/{bookId}", bookRequest.getBookId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token))
                .andExpect(status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",CoreMatchers.is(HttpStatus.UNAUTHORIZED.value())))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void bookControllerDeleteBookTestWithoutPathVariable() throws Exception
    {
        String token="Bearer-token";
        mockMvc.perform(delete("/book/deleteBook")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(NoResourceFoundException.class,result.getResolvedException()))
                .andDo(MockMvcResultHandlers.print());
    }



    @Test
    public void bookControllerSortByFieldMustReturnOKStatus() throws Exception
    {
        String token="Bearer-token";
        ResponseStructure<List<BookResponse>> responseStructure=new ResponseStructure<>(HttpStatus.OK.value(),"Books sorted successfully",List.of(bookResponse));
        given(bookService.findBooksWithSorting(anyString())).willReturn(new ResponseEntity<>(responseStructure,HttpStatus.OK));
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(adminDetails);

        mockMvc.perform(get("/book/sortBy/{field}","something")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("Authorization",token))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",CoreMatchers.is(HttpStatus.OK.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.size()",CoreMatchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].bookName",CoreMatchers.is(bookResponse.getBookName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].bookPrice",CoreMatchers.is(bookResponse.getBookPrice())))
                .andDo(MockMvcResultHandlers.print());
    }


    @Test
    public void bookControllerSortByFieldIfPathVariableIsMissing() throws Exception
    {
        String token="Bearer-token";
        mockMvc.perform(get("/book/sortBy")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("Authorization",token))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(NoResourceFoundException.class,result.getResolvedException()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void bookControllerSortByFieldTestWithoutHeader() throws Exception
    {
        mockMvc.perform(get("/book/sortBy/{field}","something")
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MissingRequestHeaderException.class,result.getResolvedException()))
                .andDo(MockMvcResultHandlers.print());
    }


    @Test
    public void bookControllerPaginationValidTest() throws Exception
    {
        String token="Bearer-token";
        ResponseStructure<List<BookResponse>> responseStructure=new ResponseStructure<>(HttpStatus.OK.value(),"Books fetched successfully",List.of(bookResponse));
        given(bookService.findBooksWithPagination(anyInt(),anyInt())).willReturn(new ResponseEntity<>(responseStructure,HttpStatus.OK));
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(adminDetails);

        mockMvc.perform(get("/book/pagination/{pageSize}",10)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("Authorization",token))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",CoreMatchers.is(HttpStatus.OK.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.size()",CoreMatchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].bookName",CoreMatchers.is(bookResponse.getBookName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].bookPrice",CoreMatchers.is(bookResponse.getBookPrice())))
                .andDo(MockMvcResultHandlers.print());
    }


    @Test
    public void bookControllerPaginationIfPageSizeIsMissing() throws Exception
    {
        String token="Bearer-token";
        mockMvc.perform(get("/book/pagination")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("Authorization",token))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(NoResourceFoundException.class,result.getResolvedException()))
                .andDo(MockMvcResultHandlers.print());
    }


    @Test
    public void bookControllerSearchQueryMustReturnOKStatus() throws Exception
    {
        String token="Bearer-token";
        ResponseStructure<List<BookResponse>> responseStructure=new ResponseStructure<>(HttpStatus.OK.value(),"Book fetched successfully",List.of(bookResponse));
        given(bookService.searchBooks(anyString())).willReturn(new ResponseEntity<>(responseStructure,HttpStatus.OK));
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(adminDetails);

        mockMvc.perform(get("/book/search/{field}","something")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("Authorization",token))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",CoreMatchers.is(HttpStatus.OK.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.size()",CoreMatchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].bookName",CoreMatchers.is(bookResponse.getBookName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].bookPrice",CoreMatchers.is(bookResponse.getBookPrice())))
                .andDo(MockMvcResultHandlers.print());
    }


    @Test
    public void bookControllerSearchQueryIfHeaderIsMissing() throws Exception
    {
        mockMvc.perform(get("/book/search/{field}","something")
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertInstanceOf(MissingRequestHeaderException.class,result.getResolvedException()))
                .andDo(MockMvcResultHandlers.print());
    }


    @Test
    public void bookControllerSearchQueryIfPathVariableIsMissing() throws Exception
    {
        String token="Bearer-token";
        mockMvc.perform(get("/book/search")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("Authorization",token))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(NoResourceFoundException.class,result.getResolvedException()))
                .andDo(MockMvcResultHandlers.print());
    }
}
