package com.example.bookstore.repository;

import com.example.bookstore.entity.Book;
import com.example.bookstore.exception.BookNotFoundException;
import com.example.bookstore.requestdto.BookRequest;
import com.example.bookstore.responsedto.BookResponse;
import com.example.bookstore.service.BookService;
import com.example.bookstore.util.ResponseStructure;
import jakarta.transaction.Transactional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BookRepositoryTests
{
    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookService bookService;

    @AfterEach
    void tearDown()
    {
        bookRepository.deleteAll();
    }

    @BeforeEach
    void setUp()
    {
        bookRepository.deleteAll();
    }

    @Test
    void addBookTest()
    {
        BookRequest bookRequest = BookRequest.builder()
                .bookName("ABCD")
                .bookPrice(789.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        ResponseEntity<ResponseStructure<BookResponse>> response=bookService.addBook(bookRequest);
        Long bookId=response.getBody().getData().getBookId();

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful());
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.CREATED.value());

        Optional<Book> book=bookRepository.findById(bookId);
        assertNotNull(book,"Book should not be null");

        Book actual=book.get();
        assertEquals(bookId,actual.getBookId());
        assertEquals(bookRequest.getBookName(),actual.getBookName());
    }


    @Test
    void getBookByIdTest()
    {
        BookRequest bookRequest = BookRequest.builder()
                .bookName("ABCD")
                .bookPrice(789.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        ResponseEntity<ResponseStructure<BookResponse>> responseStructureResponseEntity = bookService.addBook(bookRequest);
        Long bookId=responseStructureResponseEntity.getBody().getData().getBookId();

        ResponseEntity<ResponseStructure<BookResponse>> response=bookService.getBookById(bookId);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful());
        Assertions.assertThat(response.getBody().getData().getBookId()).isEqualTo(bookId);
        Assertions.assertThat(response.getBody().getData().getBookName()).isEqualTo(bookRequest.getBookName());
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void getBookByNameTest()
    {
        BookRequest bookRequest = BookRequest.builder()
                .bookName("ABCD")
                .bookPrice(789.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        bookService.addBook(bookRequest);

        ResponseEntity<ResponseStructure<BookResponse>> response=bookService.getBookByName(bookRequest.getBookName());

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful());
        Assertions.assertThat(response.getBody().getData().getBookName()).isEqualTo(bookRequest.getBookName());
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());
    }

    @Test
    void getAllBooksTest()
    {
        BookRequest bookRequest = BookRequest.builder()
                .bookName("ABCD")
                .bookPrice(789.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        BookRequest bookRequest2 = BookRequest.builder()
                .bookName("ZXCFG")
                .bookPrice(989.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        bookService.addBook(bookRequest);
        bookService.addBook(bookRequest2);

        ResponseEntity<ResponseStructure<List<BookResponse>>> response=bookService.getAllBooks();

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful());
        Assertions.assertThat(response.getBody().getData().size()).isEqualTo(2);
        Assertions.assertThat(response.getBody().getData().getFirst().getBookName()).isEqualTo(bookRequest.getBookName());
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());

        assertEquals(response.getBody().getData().get(1).getBookName(), bookRequest2.getBookName());
    }

    @Test
    void findBooksWithSortingBookName()
    {
        BookRequest bookRequest = BookRequest.builder()
                .bookName("Annabell")
                .bookPrice(789.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        BookRequest bookRequest1 = BookRequest.builder()
                .bookName("Chill")
                .bookPrice(789.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        BookRequest bookRequest2 = BookRequest.builder()
                .bookName("Zing")
                .bookPrice(989.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        bookService.addBook(bookRequest1);
        bookService.addBook(bookRequest);
        bookService.addBook(bookRequest2);

        ResponseEntity<ResponseStructure<List<BookResponse>>> response=bookService.findBooksWithSorting("bookName");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody().getData().size()).isEqualTo(3);
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());

        assertEquals(response.getBody().getData().get(1).getBookName(), bookRequest1.getBookName());

        assertEquals(response.getBody().getData().get(2).getBookName(), bookRequest2.getBookName());

        assertEquals(response.getBody().getData().getFirst().getBookName(), bookRequest.getBookName());
    }

    @Test
    void findBooksWithSortingBookPrice()
    {
        BookRequest bookRequest = BookRequest.builder()
                .bookName("Annabell")
                .bookPrice(189.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        BookRequest bookRequest1 = BookRequest.builder()
                .bookName("Chill")
                .bookPrice(389.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        BookRequest bookRequest2 = BookRequest.builder()
                .bookName("Zing")
                .bookPrice(989.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        bookService.addBook(bookRequest1);
        bookService.addBook(bookRequest);
        bookService.addBook(bookRequest2);

        ResponseEntity<ResponseStructure<List<BookResponse>>> response=bookService.findBooksWithSorting("bookPrice");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody().getData().size()).isEqualTo(3);
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());

        assertEquals(response.getBody().getData().get(1).getBookName(), bookRequest1.getBookName());
        assertEquals(response.getBody().getData().get(1).getBookPrice(), bookRequest1.getBookPrice());

        assertEquals(response.getBody().getData().get(2).getBookName(), bookRequest2.getBookName());
        assertEquals(response.getBody().getData().get(2).getBookPrice(), bookRequest2.getBookPrice());

        assertEquals(response.getBody().getData().getFirst().getBookName(), bookRequest.getBookName());
        assertEquals(response.getBody().getData().getFirst().getBookPrice(), bookRequest.getBookPrice());
    }


    @Test
    void findBooksWithSearch()
    {
        BookRequest bookRequest = BookRequest.builder()
                .bookName("Chille")
                .bookPrice(789.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        BookRequest bookRequest1 = BookRequest.builder()
                .bookName("Chill")
                .bookPrice(789.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        BookRequest bookRequest2 = BookRequest.builder()
                .bookName("Zing")
                .bookPrice(989.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Making Chilli")
                .bookQuantity(85).build();

        bookService.addBook(bookRequest1);
        bookService.addBook(bookRequest);
        bookService.addBook(bookRequest2);

        ResponseEntity<ResponseStructure<List<BookResponse>>> response=bookService.searchBooks("Chill");

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(3,response.getBody().getData().size());
    }


    @Test
    void updateBookTest()
    {
        BookRequest bookRequest = BookRequest.builder()
                .bookName("ABCD")
                .bookPrice(789.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        ResponseEntity<ResponseStructure<BookResponse>> response=bookService.addBook(bookRequest);
        Long bookId=response.getBody().getData().getBookId();

        Assertions.assertThat(response.getBody().getData().getBookName()).isEqualTo(bookRequest.getBookName());
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.CREATED.value());

        Optional<Book> book=bookRepository.findById(bookId);
        assertNotNull(book,"Book should not be null");

        Book actual=book.get();
        assertEquals(bookId,actual.getBookId());
        assertEquals(bookRequest.getBookName(),actual.getBookName());

        BookRequest updatableBook= BookRequest.builder()
                .bookName("ZXC")
                .bookPrice(489.0)
                .bookLogo("URI")
                .bookAuthor("XYZMJ")
                .bookDescription("Description")
                .bookQuantity(858).build();

        ResponseEntity<ResponseStructure<BookResponse>> findBook=bookService.updateBook(bookId,updatableBook);

        Assertions.assertThat(findBook.getStatusCode().is2xxSuccessful());
        Assertions.assertThat(findBook.getBody().getData().getBookId()).isEqualTo(bookId);
        Assertions.assertThat(findBook.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());

        BookResponse result=findBook.getBody().getData();

        assertEquals(result.getBookId(), bookId);
        assertEquals(result.getBookName(),updatableBook.getBookName());
    }

    @Test
    void deleteBookTest()
    {
        BookRequest bookRequest = BookRequest.builder()
                .bookName("ABCD")
                .bookPrice(789.0)
                .bookLogo("URL")
                .bookAuthor("XYZ")
                .bookDescription("Descript")
                .bookQuantity(85).build();

        ResponseEntity<ResponseStructure<BookResponse>> response=bookService.addBook(bookRequest);
        Long bookId=response.getBody().getData().getBookId();

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful());
        Assertions.assertThat(response.getBody().getData().getBookId()).isEqualTo(bookId);
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.CREATED.value());

        Optional<Book> book=bookRepository.findById(bookId);
        assertNotNull(book,"Book should not be null");

        Book actual=book.get();
        assertEquals(bookId,actual.getBookId());
        assertEquals(bookRequest.getBookName(),actual.getBookName());

        ResponseEntity<ResponseStructure<String>> deleteResponse=bookService.deleteBook(bookId);

        Assertions.assertThat(deleteResponse.getStatusCode().is2xxSuccessful());
        Assertions.assertThat(deleteResponse.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());

        assertThrows(BookNotFoundException.class,()->bookService.deleteBook(bookId));
    }


    @Test
    void bookRepositoryFindByBookNameTestMustReturnBook()
    {
        Book book= Book.builder()
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
    void bookRepositoryFindByBookNameTestMustThrowBookNotFoundException()
    {
        Book book= Book.builder()
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
