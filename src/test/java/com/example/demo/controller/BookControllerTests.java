package com.example.demo.controller;

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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import java.nio.charset.StandardCharsets;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

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

    private Book book;
    private BookRequestDTO bookRequestDTO;

    @BeforeEach
    public void init()
    {
        book=Book.builder()
                .bookId(1L)
                .bookName("ABCD")
                .price(789.0)
                .bookLogo("URL")
                .author("XYZ")
                .description("Descript")
                .quantity(85).build();

        bookRequestDTO=BookRequestDTO.builder()
                .bookId(1L)
                .bookName("ABCD")
                .price(789.0)
                .bookLogo("URL")
                .author("XYZ")
                .description("Descript")
                .quantity(85).build();
    }

    @Test
    public void bookController_AddBookTest_MustAddBook() throws Exception
    {
        String token="Bearer-token";
        ResponseStructure<BookResponseDTO> responseStructure=new ResponseStructure<>(HttpStatus.CREATED.value(),"Book added successfully",new BookResponseDTO());
        given(bookService.addBook(ArgumentMatchers.any(BookRequestDTO.class))).willReturn(new ResponseEntity<>(responseStructure,HttpStatus.CREATED));

        mockMvc.perform(post("/book/addBook")
                        .characterEncoding(StandardCharsets.UTF_8)
                .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization",token)
                .content(objectMapper.writeValueAsString(bookRequestDTO)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message", CoreMatchers.is(responseStructure.getMessage())))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].bookName",CoreMatchers.is(book.getBookName())));
    }
}
