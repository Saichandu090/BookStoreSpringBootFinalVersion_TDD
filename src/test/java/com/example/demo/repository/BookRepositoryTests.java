package com.example.demo.repository;

import com.example.demo.entity.Book;
import com.example.demo.exception.BookNotFoundException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
public class BookRepositoryTests
{
    @Autowired
    private BookRepository bookRepository;

    @Test
    public void bookRepository_FindByBookNameTest_MustReturnBook()
    {
        Book book= Book.builder()
                .bookId(1L)
                .bookName("Atom")
                .price(199.9)
                .author("James")
                .description("Self")
                .quantity(78)
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
                .price(199.9)
                .author("James")
                .description("Self")
                .quantity(78)
                .bookLogo("Url").build();

        Book savedBook=bookRepository.save(book);

        Assertions.assertThat(savedBook.getBookId()).isEqualTo(book.getBookId());

        org.junit.jupiter.api.Assertions.assertThrows(BookNotFoundException.class,()->bookRepository.findByBookName("Random").orElseThrow(()->new BookNotFoundException("Book not Found")));
    }
}
