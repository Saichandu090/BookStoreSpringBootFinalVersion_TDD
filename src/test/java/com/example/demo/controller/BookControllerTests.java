package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.mapper.BookMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.requestdto.BookRequestDTO;
import com.example.demo.entity.Book;
import com.example.demo.responsedto.BookResponseDTO;
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
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

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
    private BookRequestDTO bookRequestDTO;
    private BookResponseDTO bookResponseDTO;

    @BeforeEach
    public void init()
    {
        user=User.builder()
                .email("sai@gmail.com")
                .userId(1L)
                .password("saichandu090")
                .dob(LocalDate.of(2002,8,24))
                .firstName("Sai")
                .lastName("Chandu")
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

        bookRequestDTO=BookRequestDTO.builder()
                .bookId((long)789654123)
                .bookName("ABCD")
                .bookPrice(789.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        bookResponseDTO=BookResponseDTO.builder()
                .bookId(bookRequestDTO.getBookId())
                .bookLogo(bookRequestDTO.getBookLogo())
                .bookName(bookRequestDTO.getBookName())
                .bookAuthor(bookRequestDTO.getBookAuthor())
                .bookDescription(bookRequestDTO.getBookDescription())
                .bookPrice(bookRequestDTO.getBookPrice())
                .build();
    }

    @Test
    public void bookController_AddBookTest_MustReturnCreatedStatusCode() throws Exception
    {
        String token="Bearer-token";
        ResponseStructure<BookResponseDTO> responseStructure=new ResponseStructure<>(HttpStatus.CREATED.value(),"Book added successfully",bookResponseDTO);
        given(bookService.addBook(ArgumentMatchers.any(BookRequestDTO.class))).willReturn(new ResponseEntity<>(responseStructure,HttpStatus.CREATED));
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(adminDetails);

        mockMvc.perform(post("/book/addBook")
                        .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token)
                .content(objectMapper.writeValueAsString(bookRequestDTO)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",CoreMatchers.is(HttpStatus.CREATED.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.bookName",CoreMatchers.is(bookResponseDTO.getBookName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.bookId",CoreMatchers.is(bookResponseDTO.getBookId().intValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",CoreMatchers.is(responseStructure.getMessage())))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void bookController_AddBookTest_MustReturnUnauthorizedStatusCode() throws Exception
    {
        String token="Bearer-token";
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(userDetails);

        mockMvc.perform(post("/book/addBook")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token)
                        .content(objectMapper.writeValueAsString(bookRequestDTO)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",CoreMatchers.is(HttpStatus.UNAUTHORIZED.value())))
                .andDo(MockMvcResultHandlers.print());
    }

    //=========================================================================//

    @Test
    public void bookController_GetBookByName_MustReturnOKStatusCode() throws Exception
    {
        String token="Bearer-token";
        ResponseStructure<BookResponseDTO> responseStructure=new ResponseStructure<>(HttpStatus.OK.value(),"Book fetched successfully",bookResponseDTO);
        given(bookService.getBookByName(ArgumentMatchers.anyString())).willReturn(new ResponseEntity<>(responseStructure,HttpStatus.OK));
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(adminDetails);

        mockMvc.perform(get("/book/getBookByName/{bookName}",bookRequestDTO.getBookName())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",CoreMatchers.is(HttpStatus.OK.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.bookName",CoreMatchers.is(bookResponseDTO.getBookName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.bookId",CoreMatchers.is(bookResponseDTO.getBookId().intValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",CoreMatchers.is(responseStructure.getMessage())))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void bookController_GetBookByName_MustReturnUnauthorizedStatusCode() throws Exception
    {
        String token="Bearer-token";
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(userDetails);

        mockMvc.perform(get("/book/getBookByName/{bookName}",bookRequestDTO.getBookName())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",CoreMatchers.is(HttpStatus.UNAUTHORIZED.value())))
                .andDo(MockMvcResultHandlers.print());
    }

    //===============================================================//

    @Test
    public void bookController_GetBookById_MustReturnOKStatusCode() throws Exception
    {
        String token="Bearer-token";
        ResponseStructure<BookResponseDTO> responseStructure=new ResponseStructure<>(HttpStatus.OK.value(),"Book fetched successfully",bookResponseDTO);
        given(bookService.getBookById(ArgumentMatchers.anyLong())).willReturn(new ResponseEntity<>(responseStructure,HttpStatus.OK));
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(adminDetails);

        mockMvc.perform(get("/book/getBookById/{bookId}",bookRequestDTO.getBookId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("Authorization",token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",CoreMatchers.is(HttpStatus.OK.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.bookName",CoreMatchers.is(bookResponseDTO.getBookName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.bookId",CoreMatchers.is(bookResponseDTO.getBookId().intValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",CoreMatchers.is(responseStructure.getMessage())))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void bookController_GetBookById_MustReturnUnauthorizedStatusCode() throws Exception
    {
        String token="Bearer-token";
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(userDetails);

        mockMvc.perform(get("/book/getBookById/{bookId}",bookRequestDTO.getBookId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("Authorization",token))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",CoreMatchers.is(HttpStatus.UNAUTHORIZED.value())))
                .andDo(MockMvcResultHandlers.print());
    }

    //===============================================================//

    @Test
    public void bookController_GetAllBooks_MustReturnOKStatusCode() throws Exception
    {
        String token="Bearer-token";
        ResponseStructure<List<BookResponseDTO>> responseStructure=new ResponseStructure<>(HttpStatus.OK.value(),"Book fetched successfully",List.of(bookResponseDTO));
        given(bookService.getAllBooks()).willReturn(new ResponseEntity<>(responseStructure,HttpStatus.OK));
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(adminDetails);

        mockMvc.perform(get("/book/getBooks")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("Authorization",token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",CoreMatchers.is(HttpStatus.OK.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.size()",CoreMatchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].bookName",CoreMatchers.is(bookResponseDTO.getBookName())))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void bookController_GetAllBooks_MustReturnUnauthorizedStatusCode() throws Exception
    {
        String token="Bearer-token";
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(userDetails);

        mockMvc.perform(get("/book/getBooks")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("Authorization",token))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",CoreMatchers.is(HttpStatus.UNAUTHORIZED.value())))
                .andDo(MockMvcResultHandlers.print());
    }

    //=================================================//

    @Test
    public void bookController_UpdateBook_MustReturnOKStatusCode() throws Exception
    {
        String token="Bearer-token";
        ResponseStructure<BookResponseDTO> responseStructure=new ResponseStructure<>(HttpStatus.OK.value(),"Book Updated successfully",bookResponseDTO);
        given(bookService.updateBook(ArgumentMatchers.anyLong(),ArgumentMatchers.any(BookRequestDTO.class))).willReturn(new ResponseEntity<>(responseStructure,HttpStatus.OK));
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(adminDetails);

        mockMvc.perform(put("/book/updateBook/{bookId}",bookRequestDTO.getBookId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token)
                        .content(objectMapper.writeValueAsString(bookRequestDTO)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",CoreMatchers.is(HttpStatus.OK.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.bookName",CoreMatchers.is(bookResponseDTO.getBookName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.bookId",CoreMatchers.is(bookResponseDTO.getBookId().intValue())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",CoreMatchers.is(responseStructure.getMessage())))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void bookController_UpdateBook_MustReturnUnauthorizedStatusCode() throws Exception
    {
        String token="Bearer-token";
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(userDetails);

        mockMvc.perform(put("/book/updateBook/{bookId}",bookRequestDTO.getBookId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token)
                        .content(objectMapper.writeValueAsString(bookRequestDTO)))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",CoreMatchers.is(HttpStatus.UNAUTHORIZED.value())))
                .andDo(MockMvcResultHandlers.print());
    }

    //=================================================//

    @Test
    public void bookController_DeleteBook_MustReturnOKStatusCode() throws Exception
    {
        String token="Bearer-token";
        ResponseStructure<String> responseStructure=new ResponseStructure<>(HttpStatus.OK.value(),"Book deleted successfully","Success");
        given(bookService.deleteBook(ArgumentMatchers.anyLong())).willReturn(new ResponseEntity<>(responseStructure,HttpStatus.OK));
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(adminDetails);

        mockMvc.perform(delete("/book/deleteBook/{bookId}",bookRequestDTO.getBookId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",CoreMatchers.is(HttpStatus.OK.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data",CoreMatchers.is(responseStructure.getData())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message",CoreMatchers.is(responseStructure.getMessage())))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void bookController_DeleteBook_MustReturnUnauthorizedStatusCode() throws Exception
    {
        String token="Bearer-token";
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(userDetails);

        mockMvc.perform(delete("/book/deleteBook/{bookId}",bookRequestDTO.getBookId())
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",CoreMatchers.is(HttpStatus.UNAUTHORIZED.value())))
                .andDo(MockMvcResultHandlers.print());
    }

    //=======================================================//

    @Test
    public void bookController_SortByBookName_MustReturnOKStatus() throws Exception
    {
        String token="Bearer-token";
        ResponseStructure<List<BookResponseDTO>> responseStructure=new ResponseStructure<>(HttpStatus.OK.value(),"Books sorted successfully",List.of(bookResponseDTO));
        given(bookService.sortByBookName()).willReturn(new ResponseEntity<>(responseStructure,HttpStatus.OK));
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(adminDetails);

        mockMvc.perform(get("/book/sortByBookName")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("Authorization",token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",CoreMatchers.is(HttpStatus.OK.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.size()",CoreMatchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].bookName",CoreMatchers.is(bookResponseDTO.getBookName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].bookPrice",CoreMatchers.is(bookResponseDTO.getBookPrice())))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void bookController_SortByBookName_MustReturnUnauthorizedStatus() throws Exception
    {
        String token="Bearer-token";
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(userDetails);

        mockMvc.perform(get("/book/sortByBookName")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("Authorization",token))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",CoreMatchers.is(HttpStatus.UNAUTHORIZED.value())))
                .andDo(MockMvcResultHandlers.print());
    }

    //====================================================//

    @Test
    public void bookController_SortByBookPrice_MustReturnOKStatus() throws Exception
    {
        String token="Bearer-token";
        ResponseStructure<List<BookResponseDTO>> responseStructure=new ResponseStructure<>(HttpStatus.OK.value(),"Books sorted successfully",List.of(bookResponseDTO));
        given(bookService.sortByBookPrice()).willReturn(new ResponseEntity<>(responseStructure,HttpStatus.OK));
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(adminDetails);

        mockMvc.perform(get("/book/sortByBookPrice")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("Authorization",token))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",CoreMatchers.is(HttpStatus.OK.value())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.size()",CoreMatchers.is(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].bookName",CoreMatchers.is(bookResponseDTO.getBookName())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].bookPrice",CoreMatchers.is(bookResponseDTO.getBookPrice())))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    public void bookController_SortByBookPrice_MustReturnUnauthorizedStatus() throws Exception
    {
        String token="Bearer-token";
        when(userMapper.validateUserToken(Mockito.anyString())).thenReturn(userDetails);

        mockMvc.perform(get("/book/sortByBookPrice")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("Authorization",token))
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status",CoreMatchers.is(HttpStatus.UNAUTHORIZED.value())))
                .andDo(MockMvcResultHandlers.print());
    }
}
