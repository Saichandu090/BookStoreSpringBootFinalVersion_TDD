package com.example.demo.repository;

import com.example.demo.entity.Book;
import com.example.demo.exception.BookNotFoundException;
import com.example.demo.requestdto.BookRequestDto;
import com.example.demo.responsedto.BookResponseDto;
import com.example.demo.service.BookService;
import com.example.demo.util.ResponseStructure;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2")
public class BookRepositoryTests
{
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookService bookService;

    @Test
    public void addBookTest()
    {
        BookRequestDto bookRequestDto=BookRequestDto.builder()
                .bookId((long)789654123)
                .bookName("ABCD")
                .bookPrice(789.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        ResponseEntity<ResponseStructure<BookResponseDto>> response=bookService.addBook(bookRequestDto);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful());
        Assertions.assertThat(response.getBody().getData().getBookId()).isEqualTo(bookRequestDto.getBookId());
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.CREATED.value());

        Optional<Book> book=bookRepository.findById(bookRequestDto.getBookId());
        assertNotNull(book,"Book should not be null");

        Book actual=book.get();
        assertEquals(bookRequestDto.getBookId(),actual.getBookId());
        assertEquals(bookRequestDto.getBookName(),actual.getBookName());
    }


    @Test
    public void getBookByIdTest()
    {
        BookRequestDto bookRequestDto=BookRequestDto.builder()
                .bookId((long)789654123)
                .bookName("ABCD")
                .bookPrice(789.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        bookService.addBook(bookRequestDto);

        ResponseEntity<ResponseStructure<BookResponseDto>> response=bookService.getBookById(bookRequestDto.getBookId());

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful());
        Assertions.assertThat(response.getBody().getData().getBookId()).isEqualTo(bookRequestDto.getBookId());
        Assertions.assertThat(response.getBody().getData().getBookName()).isEqualTo(bookRequestDto.getBookName());
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void getBookByNameTest()
    {
        BookRequestDto bookRequestDto=BookRequestDto.builder()
                .bookId((long)789654123)
                .bookName("ABCD")
                .bookPrice(789.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        bookService.addBook(bookRequestDto);

        ResponseEntity<ResponseStructure<BookResponseDto>> response=bookService.getBookByName(bookRequestDto.getBookName());

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful());
        Assertions.assertThat(response.getBody().getData().getBookId()).isEqualTo(bookRequestDto.getBookId());
        Assertions.assertThat(response.getBody().getData().getBookName()).isEqualTo(bookRequestDto.getBookName());
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    public void getAllBooksTest()
    {
        BookRequestDto bookRequestDto=BookRequestDto.builder()
                .bookId((long)789654123)
                .bookName("ABCD")
                .bookPrice(789.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        BookRequestDto bookRequestDto2=BookRequestDto.builder()
                .bookId((long)789654126)
                .bookName("ZXCFG")
                .bookPrice(989.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        bookService.addBook(bookRequestDto);
        bookService.addBook(bookRequestDto2);

        ResponseEntity<ResponseStructure<List<BookResponseDto>>> response=bookService.getAllBooks();

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful());
        Assertions.assertThat(response.getBody().getData().size()).isEqualTo(2);
        Assertions.assertThat(response.getBody().getData().getFirst().getBookName()).isEqualTo(bookRequestDto.getBookName());
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());

        assertEquals(response.getBody().getData().get(1).getBookId(),bookRequestDto2.getBookId());
        assertEquals(response.getBody().getData().get(1).getBookName(),bookRequestDto2.getBookName());
    }

    @Test
    public void sortByBookNameTest()
    {
        BookRequestDto bookRequestDto=BookRequestDto.builder()
                .bookId((long)789654123)
                .bookName("Annabell")
                .bookPrice(789.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        BookRequestDto bookRequestDto1=BookRequestDto.builder()
                .bookId((long)789654121)
                .bookName("Chill")
                .bookPrice(789.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        BookRequestDto bookRequestDto2=BookRequestDto.builder()
                .bookId((long)789654126)
                .bookName("Zing")
                .bookPrice(989.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        bookService.addBook(bookRequestDto1);
        bookService.addBook(bookRequestDto);
        bookService.addBook(bookRequestDto2);

        ResponseEntity<ResponseStructure<List<BookResponseDto>>> response=bookService.sortByBookName();

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody().getData().size()).isEqualTo(3);
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());

        assertEquals(response.getBody().getData().get(1).getBookId(),bookRequestDto1.getBookId());
        assertEquals(response.getBody().getData().get(1).getBookName(),bookRequestDto1.getBookName());

        assertEquals(response.getBody().getData().get(2).getBookId(),bookRequestDto2.getBookId());
        assertEquals(response.getBody().getData().get(2).getBookName(),bookRequestDto2.getBookName());

        assertEquals(response.getBody().getData().getFirst().getBookId(),bookRequestDto.getBookId());
        assertEquals(response.getBody().getData().getFirst().getBookName(),bookRequestDto.getBookName());
    }

    @Test
    public void sortByBookPriceTest()
    {
        BookRequestDto bookRequestDto=BookRequestDto.builder()
                .bookId((long)789654123)
                .bookName("Annabell")
                .bookPrice(189.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        BookRequestDto bookRequestDto1=BookRequestDto.builder()
                .bookId((long)789654121)
                .bookName("Chill")
                .bookPrice(389.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        BookRequestDto bookRequestDto2=BookRequestDto.builder()
                .bookId((long)789654126)
                .bookName("Zing")
                .bookPrice(989.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        bookService.addBook(bookRequestDto1);
        bookService.addBook(bookRequestDto);
        bookService.addBook(bookRequestDto2);

        ResponseEntity<ResponseStructure<List<BookResponseDto>>> response=bookService.sortByBookPrice();

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody().getData().size()).isEqualTo(3);
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());

        assertEquals(response.getBody().getData().get(1).getBookId(),bookRequestDto1.getBookId());
        assertEquals(response.getBody().getData().get(1).getBookName(),bookRequestDto1.getBookName());
        assertEquals(response.getBody().getData().get(1).getBookPrice(),bookRequestDto1.getBookPrice());

        assertEquals(response.getBody().getData().get(2).getBookId(),bookRequestDto2.getBookId());
        assertEquals(response.getBody().getData().get(2).getBookName(),bookRequestDto2.getBookName());
        assertEquals(response.getBody().getData().get(2).getBookPrice(),bookRequestDto2.getBookPrice());

        assertEquals(response.getBody().getData().getFirst().getBookId(),bookRequestDto.getBookId());
        assertEquals(response.getBody().getData().getFirst().getBookName(),bookRequestDto.getBookName());
        assertEquals(response.getBody().getData().getFirst().getBookPrice(),bookRequestDto.getBookPrice());
    }


    @Test
    public void updateBookTest()
    {
        BookRequestDto bookRequestDto=BookRequestDto.builder()
                .bookId((long)789654123)
                .bookName("ABCD")
                .bookPrice(789.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        ResponseEntity<ResponseStructure<BookResponseDto>> response=bookService.addBook(bookRequestDto);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful());
        Assertions.assertThat(response.getBody().getData().getBookId()).isEqualTo(bookRequestDto.getBookId());
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.CREATED.value());

        Optional<Book> book=bookRepository.findById(bookRequestDto.getBookId());
        assertNotNull(book,"Book should not be null");

        Book actual=book.get();
        assertEquals(bookRequestDto.getBookId(),actual.getBookId());
        assertEquals(bookRequestDto.getBookName(),actual.getBookName());

        BookRequestDto updatableBook=BookRequestDto.builder()
                .bookName("ZXC")
                .bookPrice(489.0)
                .bookLogo("URI")
                .bookAuthor("XYZMJ")
                .bookDescription("Description")
                .bookQuantity(858).build();

        ResponseEntity<ResponseStructure<BookResponseDto>> findBook=bookService.updateBook(bookRequestDto.getBookId(),updatableBook);

        Assertions.assertThat(findBook.getStatusCode().is2xxSuccessful());
        Assertions.assertThat(findBook.getBody().getData().getBookId()).isEqualTo(bookRequestDto.getBookId());
        Assertions.assertThat(findBook.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());

        BookResponseDto result=findBook.getBody().getData();

        assertEquals(result.getBookId(),bookRequestDto.getBookId());
        assertEquals(result.getBookName(),updatableBook.getBookName());
    }

    @Test
    public void deleteBookTest()
    {
        BookRequestDto bookRequestDto=BookRequestDto.builder()
                .bookId((long)789654123)
                .bookName("ABCD")
                .bookPrice(789.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        ResponseEntity<ResponseStructure<BookResponseDto>> response=bookService.addBook(bookRequestDto);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful());
        Assertions.assertThat(response.getBody().getData().getBookId()).isEqualTo(bookRequestDto.getBookId());
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.CREATED.value());

        Optional<Book> book=bookRepository.findById(bookRequestDto.getBookId());
        assertNotNull(book,"Book should not be null");

        Book actual=book.get();
        assertEquals(bookRequestDto.getBookId(),actual.getBookId());
        assertEquals(bookRequestDto.getBookName(),actual.getBookName());

        ResponseEntity<ResponseStructure<String>> deleteResponse=bookService.deleteBook(bookRequestDto.getBookId());

        Assertions.assertThat(deleteResponse.getStatusCode().is2xxSuccessful());
        Assertions.assertThat(deleteResponse.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());

        assertAll(()->bookService.deleteBook(bookRequestDto.getBookId()));

        ResponseEntity<ResponseStructure<BookResponseDto>> findResponse=bookService.getBookById(bookRequestDto.getBookId());

        Assertions.assertThat(findResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertEquals(findResponse.getBody().getStatus(),404,"Should not be in database");
    }


    @Test
    public void bookRepository_FindByBookNameTest_MustReturnBook()
    {
        Book book= Book.builder()
                .bookId(1L)
                .bookName("Atom")
                .bookPrice(199.9)
                .bookAuthor("James")
                .bookDescription("Self")
                .bookQuantity(78)
                .bookLogo("Url").build();

        Book savedBook=bookRepository.save(book);

        Assertions.assertThat(savedBook.getBookId()).isEqualTo(book.getBookId());

        Book findBook=bookRepository.findByBookName(book.getBookName()).orElseThrow(()->new BookNotFoundException("Book not Found"));
        Assertions.assertThat(findBook.getBookId()).isEqualTo(book.getBookId());
        Assertions.assertThat(findBook.getBookName()).isEqualTo(book.getBookName());
    }


    @Test
    public void bookRepository_FindByBookNameTest_MustThrowBookNotFoundException()
    {
        Book book= Book.builder()
                .bookId(1L)
                .bookName("Atom")
                .bookPrice(199.9)
                .bookAuthor("James")
                .bookDescription("Self")
                .bookQuantity(78)
                .bookLogo("Url").build();

        Book savedBook=bookRepository.save(book);

        Assertions.assertThat(savedBook.getBookId()).isEqualTo(book.getBookId());

        org.junit.jupiter.api.Assertions.assertThrows(BookNotFoundException.class,()->bookRepository.findByBookName("Random").orElseThrow(()->new BookNotFoundException("Book not Found")));
    }
}
