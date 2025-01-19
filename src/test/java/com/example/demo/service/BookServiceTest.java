package com.example.demo.service;

import com.example.demo.entity.Book;
import com.example.demo.exception.BookNotFoundException;
import com.example.demo.exception.InvalidPaginationException;
import com.example.demo.exception.InvalidSortingFieldException;
import com.example.demo.mapper.BookMapper;
import com.example.demo.repository.BookRepository;
import com.example.demo.requestdto.BookRequestDto;
import com.example.demo.responsedto.BookResponseDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

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
    private BookRequestDto bookRequestDTO;
    private BookResponseDto bookResponseDTO;

    @BeforeEach
    public void init()
    {
        bookRequestDTO= BookRequestDto.builder()
                .bookId(1L)
                .bookName("Jenes")
                .bookPrice(789.99)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        book=Book.builder()
                .bookId(1L)
                .bookLogo("URL")
                .bookName("Jenny")
                .bookAuthor("Chandu")
                .bookDescription("Description")
                .bookPrice(789.49)
                .cartBookQuantity(0)
                .build();

        bookResponseDTO= BookResponseDto.builder()
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

        ResponseEntity<ResponseStructure<BookResponseDto>> response=bookService.addBook(bookRequestDTO);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(response.getBody().getData().getBookId()).isEqualTo(bookResponseDTO.getBookId());
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.CREATED.value());
        Assertions.assertThat(response.getBody().getMessage()).isEqualTo(response.getBody().getMessage());
    }



    @Test
    public void bookService_GetBookByName_MustReturnOKStatusCode()
    {
        when(bookRepository.findByBookName(Mockito.anyString())).thenReturn(Optional.of(book));

        ResponseEntity<ResponseStructure<BookResponseDto>> response=bookService.getBookByName(bookRequestDTO.getBookName());

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody().getData().getBookId()).isEqualTo(bookResponseDTO.getBookId());
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());
        Assertions.assertThat(response.getBody().getMessage()).isEqualTo(response.getBody().getMessage());
    }



    @Test
    public void bookService_GetBookByName_MustThrowBookNotFoundException()
    {
        when(bookRepository.findByBookName(Mockito.anyString())).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class,()->bookService.getBookByName(bookRequestDTO.getBookName()));

        verify(bookRepository,times(1)).findByBookName(anyString());
    }



    @Test
    public void bookService_GetBookById_MustReturnOKStatusCode()
    {
        when(bookRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(book));

        ResponseEntity<ResponseStructure<BookResponseDto>> response=bookService.getBookById(bookRequestDTO.getBookId());

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody().getData().getBookId()).isEqualTo(bookResponseDTO.getBookId());
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());
        Assertions.assertThat(response.getBody().getMessage()).isEqualTo(response.getBody().getMessage());
    }



    @Test
    public void bookService_GetBookById_MustReturnNotFoundStatusCode()
    {
        when(bookRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class,()->bookService.getBookById(bookRequestDTO.getBookId()));

        verify(bookRepository,times(1)).findById(anyLong());
    }


    @Test
    public void bookService_GetAllBooks_MustReturnOKStatusCode()
    {
        when(bookRepository.findAll()).thenReturn(List.of(book));

        ResponseEntity<ResponseStructure<List<BookResponseDto>>> response=bookService.getAllBooks();

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());
        Assertions.assertThat(response.getBody().getData().size()).isEqualTo(1);
        Assertions.assertThat(response.getBody().getData().getFirst().getBookId()).isEqualTo(book.getBookId().intValue());
    }



    @Test
    public void bookService_GetAllBooks_MustReturnNoContentStatusCode()
    {
        when(bookRepository.findAll()).thenReturn(List.of());

        ResponseEntity<ResponseStructure<List<BookResponseDto>>> response=bookService.getAllBooks();

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        Assertions.assertThat(Objects.requireNonNull(response.getBody()).getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }



    @Test
    public void bookService_UpdateBook_MustReturnOKStatusCode()
    {
        when(bookRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(book));
        when(bookRepository.save(Mockito.any(Book.class))).thenReturn(book);

        ResponseEntity<ResponseStructure<BookResponseDto>> response=bookService.updateBook(bookRequestDTO.getBookId(),bookRequestDTO);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());
        Assertions.assertThat(response.getBody().getData().getBookName()).isEqualTo(book.getBookName());
        Assertions.assertThat(response.getBody().getData().getBookId()).isEqualTo(book.getBookId().intValue());
    }



    @Test
    public void bookService_UpdateBook_MustReturnNotFoundStatusCode()
    {
        when(bookRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class,()->bookService.updateBook(bookRequestDTO.getBookId(),bookRequestDTO));

        verify(bookRepository,times(1)).findById(anyLong());
    }



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

        assertThrows(BookNotFoundException.class,()->bookService.deleteBook(bookRequestDTO.getBookId()));

        Mockito.verify(bookRepository,times(0)).delete(book);
    }


    @Test
    public void bookService_FindBooksWithSorting_MustReturnOKStatusCode_BookPrice()
    {
        Book first=Book.builder()
                .bookId(2L)
                .bookLogo("My url")
                .bookName("Lawsuit")
                .bookAuthor("Carley")
                .bookDescription("Law")
                .bookPrice(220.99)
                .cartBookQuantity(0)
                .build();

        Book second=Book.builder()
                .bookId(3L)
                .bookLogo("Man")
                .bookName("Jimmy man")
                .bookAuthor("Brother")
                .bookDescription("Family")
                .bookPrice(225.00)
                .cartBookQuantity(0)
                .build();

        Book third=Book.builder()
                .bookId(4L)
                .bookLogo("URL")
                .bookName("James miggel")
                .bookAuthor("Chuck")
                .bookDescription("Descript")
                .bookPrice(99.90)
                .cartBookQuantity(0)
                .build();

        List<Book> books=new ArrayList<>();
        books.add(third);
        books.add(first);
        books.add(second);

        when(bookRepository.findAll(Sort.by(Sort.Direction.ASC,"bookPrice"))).thenReturn(books);

        ResponseEntity<ResponseStructure<List<BookResponseDto>>> response=bookService.findBooksWithSorting("bookPrice");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(Objects.requireNonNull(response.getBody()).getStatus()).isEqualTo(HttpStatus.OK.value());
        Assertions.assertThat(response.getBody().getData().size()).isEqualTo(3);
        assertEquals(4,response.getBody().getData().getFirst().getBookId());
        assertEquals(2,response.getBody().getData().get(1).getBookId());
        assertEquals(3,response.getBody().getData().get(2).getBookId());
    }


    @Test
    public void bookService_FindBooksWithSorting_IfFieldIsInvalid()
    {
        assertThrows(InvalidSortingFieldException.class,()->bookService.findBooksWithSorting("Test"));
    }



    @Test
    public void bookService_Pagination_ValidTest()
    {
        List<Book> books = IntStream.range(1, 20)
                .mapToObj(i -> Book.builder().bookId((long) i).build())
                .collect(Collectors.toList());

        Page<Book> bookPage = new PageImpl<>(books.subList(0,10), PageRequest.of(0, 10), books.size());
        when(bookRepository.findAll(PageRequest.of(0,10))).thenReturn(bookPage);

        ResponseEntity<ResponseStructure<List<BookResponseDto>>> response=bookService.findBooksWithPagination(0,10);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertEquals(10,response.getBody().getData().size(),"Ensuring the size of books per page provided");
        assertEquals(1L, response.getBody().getData().get(0).getBookId(),"Expecting that first page should start with 1");
        assertEquals(10L, response.getBody().getData().get(9).getBookId(),"Expecting that first page should end with 10");
    }


    @Test
    public void bookService_Pagination_SecondPageTest()
    {
        List<Book> books = IntStream.rangeClosed(1, 20)
                .mapToObj(i -> Book.builder().bookId((long) i).build())
                .collect(Collectors.toList());

        Page<Book> bookPage = new PageImpl<>(books.subList(10, 20), PageRequest.of(1, 10), books.size());
        when(bookRepository.findAll(PageRequest.of(1, 10))).thenReturn(bookPage);

        ResponseEntity<ResponseStructure<List<BookResponseDto>>> response = bookService.findBooksWithPagination(1, 10);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertEquals(10, response.getBody().getData().size());
        assertEquals(11L, response.getBody().getData().get(0).getBookId(),"Expecting that second page should start with 11");
        assertEquals(20L, response.getBody().getData().get(9).getBookId(),"Expecting that second page should end with 20");
    }


    @Test
    public void bookService_Pagination_IfNoBooksToDisplay()
    {
        when(bookRepository.findAll(PageRequest.of(1, 10))).thenReturn(Page.empty());

        ResponseEntity<ResponseStructure<List<BookResponseDto>>> response = bookService.findBooksWithPagination(1, 10);

        assertEquals(HttpStatus.NO_CONTENT,response.getStatusCode());
    }

    @Test
    public void bookService_Pagination_IfGivenInvalidPageNumber()
    {
        assertThrows(InvalidPaginationException.class,()->bookService.findBooksWithPagination(-1, 10));
    }


    @Test
    public void bookService_SearchQuery_ValidTest()
    {
        List<Book> books = IntStream.range(1, 20)
                .mapToObj(i -> Book.builder().bookId((long) i).build())
                .collect(Collectors.toList());

        when(bookRepository.findByBookNameContainingOrBookAuthorContainingOrBookDescriptionContaining(anyString(),anyString(),anyString())).thenReturn(books);
        ResponseEntity<ResponseStructure<List<BookResponseDto>>> response=bookService.searchBooks("anything");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertEquals("Books matching the query: anything",response.getBody().getMessage());
    }


    @Test
    public void bookService_SearchQuery_IfNothingMatches()
    {
        when(bookRepository.findByBookNameContainingOrBookAuthorContainingOrBookDescriptionContaining(anyString(),anyString(),anyString())).thenReturn(List.of());
        ResponseEntity<ResponseStructure<List<BookResponseDto>>> response=bookService.searchBooks("anything");
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertEquals("Books are empty",response.getBody().getMessage());
    }

    @Test
    public void bookService_SearchQuery_IfQueryIsEmpty()
    {
        ResponseEntity<ResponseStructure<List<BookResponseDto>>> response=bookService.searchBooks("");
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertEquals("Books are empty",response.getBody().getMessage());
    }
}