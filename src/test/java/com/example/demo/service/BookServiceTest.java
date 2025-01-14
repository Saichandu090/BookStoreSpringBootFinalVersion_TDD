package com.example.demo.service;

import com.example.demo.entity.Book;
import com.example.demo.mapper.BookMapper;
import com.example.demo.repository.BookRepository;
import com.example.demo.requestdto.BookRequestDTO;
import com.example.demo.responsedto.BookResponseDTO;
import com.example.demo.serviceimpl.BookServiceImpl;
import com.example.demo.util.ResponseStructure;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest
{
    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    @MockitoBean
    private BookMapper bookMapper;

    private Book book;
    private BookRequestDTO bookRequestDTO;
    private BookResponseDTO bookResponseDTO;

    @BeforeEach
    public void init()
    {
        bookRequestDTO=BookRequestDTO.builder()
                .bookId((long)789654123)
                .bookName("Jenes")
                .bookPrice(789.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        book=Book.builder()
                .bookId(bookRequestDTO.getBookId())
                .bookLogo(bookRequestDTO.getBookLogo())
                .bookName(bookRequestDTO.getBookName())
                .bookAuthor(bookRequestDTO.getBookAuthor())
                .bookDescription(bookRequestDTO.getBookDescription())
                .bookPrice(bookRequestDTO.getBookPrice())
                .cartBookQuantity(0)
                .build();

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
    public void bookService_AddBook_MustReturnCreatedStatus()
    {
        when(bookRepository.save(Mockito.any(Book.class))).thenReturn(book);

        ResponseEntity<ResponseStructure<BookResponseDTO>> response=bookService.addBook(bookRequestDTO);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(response.getBody().getData().getBookId()).isEqualTo(bookResponseDTO.getBookId());
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.CREATED.value());
        Assertions.assertThat(response.getBody().getMessage()).isEqualTo(response.getBody().getMessage());
    }

    @Test
    public void bookService_AddBook_MustThrowException()
    {
        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,()->bookService.addBook(null));
    }

    //================================================//

    @Test
    public void bookService_GetBookByName_MustReturnOKStatusCode()
    {
        when(bookRepository.findByBookName(Mockito.anyString())).thenReturn(Optional.of(book));

        ResponseEntity<ResponseStructure<BookResponseDTO>> response=bookService.getBookByName(bookRequestDTO.getBookName());

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody().getData().getBookId()).isEqualTo(bookResponseDTO.getBookId());
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());
        Assertions.assertThat(response.getBody().getMessage()).isEqualTo(response.getBody().getMessage());
    }

    @Test
    public void bookService_GetBookByName_MustReturnNotFoundStatusCode()
    {
        when(bookRepository.findByBookName(Mockito.anyString())).thenReturn(Optional.empty());

        ResponseEntity<ResponseStructure<BookResponseDTO>> response=bookService.getBookByName(bookRequestDTO.getBookName());

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    //================================================//

    @Test
    public void bookService_GetBookById_MustReturnOKStatusCode()
    {
        when(bookRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(book));

        ResponseEntity<ResponseStructure<BookResponseDTO>> response=bookService.getBookById(bookRequestDTO.getBookId());

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody().getData().getBookId()).isEqualTo(bookResponseDTO.getBookId());
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());
        Assertions.assertThat(response.getBody().getMessage()).isEqualTo(response.getBody().getMessage());
    }

    @Test
    public void bookService_GetBookById_MustReturnNotFoundStatusCode()
    {
        when(bookRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        ResponseEntity<ResponseStructure<BookResponseDTO>> response=bookService.getBookById(bookRequestDTO.getBookId());

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
    }

    //================================================//

    @Test
    public void bookService_GetAllBooks_MustReturnOKStatusCode()
    {
        when(bookRepository.findAll()).thenReturn(List.of(book));

        ResponseEntity<ResponseStructure<List<BookResponseDTO>>> response=bookService.getAllBooks();

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());
        Assertions.assertThat(response.getBody().getData().size()).isEqualTo(1);
        Assertions.assertThat(response.getBody().getData().getFirst().getBookId()).isEqualTo(book.getBookId().intValue());
    }

    @Test
    public void bookService_GetAllBooks_MustReturnNoContentStatusCode()
    {
        when(bookRepository.findAll()).thenReturn(List.of());

        ResponseEntity<ResponseStructure<List<BookResponseDTO>>> response=bookService.getAllBooks();

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        Assertions.assertThat(Objects.requireNonNull(response.getBody()).getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    //================================================//

    @Test
    public void bookService_UpdateBook_MustReturnOKStatusCode()
    {
        when(bookRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(book));
        when(bookRepository.save(Mockito.any(Book.class))).thenReturn(book);

        ResponseEntity<ResponseStructure<BookResponseDTO>> response=bookService.updateBook(bookRequestDTO.getBookId(),bookRequestDTO);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());
        Assertions.assertThat(response.getBody().getData().getBookName()).isEqualTo(book.getBookName());
        Assertions.assertThat(response.getBody().getData().getBookId()).isEqualTo(book.getBookId().intValue());
    }

    @Test
    public void bookService_UpdateBook_MustReturnNotFoundStatusCode()
    {
        when(bookRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        ResponseEntity<ResponseStructure<BookResponseDTO>> response=bookService.updateBook(bookRequestDTO.getBookId(),bookRequestDTO);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        Assertions.assertThat(response.getBody().getData()).isNull();
    }


    //==================================================//

    @Test
    public void bookService_DeleteBook_MustReturnOKStatusCode()
    {
        when(bookRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(book));

        ResponseEntity<ResponseStructure<String>> response=bookService.deleteBook(bookRequestDTO.getBookId());

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());
        Assertions.assertThat(response.getBody().getData()).isEqualTo("Success");

        Mockito.verify(bookRepository,times(1)).delete(book);
    }

    @Test
    public void bookService_DeleteBook_MustReturnNotFoundStatusCode()
    {
        when(bookRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        ResponseEntity<ResponseStructure<String>> response=bookService.deleteBook(bookRequestDTO.getBookId());

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        Assertions.assertThat(response.getBody().getData()).isEqualTo("Failure");

        Mockito.verify(bookRepository,times(0)).delete(book);
    }

    //=================================================//

    @Test
    public void bookService_SortByBookName_MustReturnOKStatusCode()
    {
        Book first=Book.builder()
                        .bookId(2L)
            .bookLogo(bookRequestDTO.getBookLogo())
            .bookName("James man")
            .bookAuthor(bookRequestDTO.getBookAuthor())
            .bookDescription(bookRequestDTO.getBookDescription())
            .bookPrice(bookRequestDTO.getBookPrice())
            .cartBookQuantity(0)
            .build();

        List<Book> books=new ArrayList<>();
        books.add(book);
        books.add(first);

        when(bookRepository.findAll()).thenReturn(books);

        ResponseEntity<ResponseStructure<List<BookResponseDTO>>> response=bookService.sortByBookName();

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(Objects.requireNonNull(response.getBody()).getStatus()).isEqualTo(HttpStatus.OK.value());
        Assertions.assertThat(response.getBody().getData().size()).isEqualTo(2);
        Assertions.assertThat(response.getBody().getData().get(1).getBookId()).isEqualTo(book.getBookId().intValue());
        Assertions.assertThat(response.getBody().getData().getFirst().getBookName()).isEqualTo(first.getBookName());
        Assertions.assertThat(response.getBody().getData().getFirst().getBookId()).isEqualTo(first.getBookId().intValue());
    }

    @Test
    public void bookService_SortByBookName_MustReturnNoContentStatusCode()
    {
        when(bookRepository.findAll()).thenReturn(List.of());

        ResponseEntity<ResponseStructure<List<BookResponseDTO>>> response=bookService.sortByBookName();

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        Assertions.assertThat(Objects.requireNonNull(response.getBody()).getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }

    //==================================================//

    @Test
    public void bookService_SortByBookPrice_MustReturnOKStatusCode()
    {
        Book first=Book.builder()
                .bookId(2L)
                .bookLogo(bookRequestDTO.getBookLogo())
                .bookName("James man")
                .bookAuthor(bookRequestDTO.getBookAuthor())
                .bookDescription(bookRequestDTO.getBookDescription())
                .bookPrice(120.56)
                .cartBookQuantity(0)
                .build();

        Book second=Book.builder()
                .bookId(2L)
                .bookLogo(bookRequestDTO.getBookLogo())
                .bookName("James man")
                .bookAuthor(bookRequestDTO.getBookAuthor())
                .bookDescription(bookRequestDTO.getBookDescription())
                .bookPrice(220.56)
                .cartBookQuantity(0)
                .build();

        List<Book> books=new ArrayList<>();
        books.add(second);
        books.add(book);
        books.add(first);

        when(bookRepository.findAll()).thenReturn(books);

        ResponseEntity<ResponseStructure<List<BookResponseDTO>>> response=bookService.sortByBookPrice();

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(Objects.requireNonNull(response.getBody()).getStatus()).isEqualTo(HttpStatus.OK.value());
        Assertions.assertThat(response.getBody().getData().size()).isEqualTo(3);
        Assertions.assertThat(response.getBody().getData().getFirst().getBookName()).isEqualTo(first.getBookName());
        Assertions.assertThat(response.getBody().getData().getFirst().getBookId()).isEqualTo(first.getBookId().intValue());
        Assertions.assertThat(response.getBody().getData().get(1).getBookId()).isEqualTo(second.getBookId().intValue());
        Assertions.assertThat(response.getBody().getData().get(1).getBookName()).isEqualTo(second.getBookName());
        Assertions.assertThat(response.getBody().getData().get(2).getBookId()).isEqualTo(book.getBookId().intValue());
        Assertions.assertThat(response.getBody().getData().get(2).getBookName()).isEqualTo(book.getBookName());
    }

    @Test
    public void bookService_SortByBookPrice_MustReturnNoContentStatusCode()
    {
        when(bookRepository.findAll()).thenReturn(List.of());

        ResponseEntity<ResponseStructure<List<BookResponseDTO>>> response=bookService.sortByBookPrice();

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        Assertions.assertThat(Objects.requireNonNull(response.getBody()).getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }
}