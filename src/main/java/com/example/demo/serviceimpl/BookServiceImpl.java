package com.example.demo.serviceimpl;

import com.example.demo.entity.Book;
import com.example.demo.exception.BookNotFoundException;
import com.example.demo.mapper.BookMapper;
import com.example.demo.requestdto.BookRequestDto;
import com.example.demo.repository.BookRepository;
import com.example.demo.responsedto.BookResponseDto;
import com.example.demo.service.BookService;
import com.example.demo.util.ResponseStructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class BookServiceImpl implements BookService
{
    @Autowired
    private BookRepository bookRepository;

    private final BookMapper bookMapper=new BookMapper();

    @Override
    public ResponseEntity<ResponseStructure<BookResponseDto>> addBook(BookRequestDto bookRequestDTO)
    {
        Book book=bookMapper.addBook(bookRequestDTO);
        Book savedBook=bookRepository.save(book);
        return bookMapper.mapToSuccessAddBook(savedBook);
    }


    @Override
    public ResponseEntity<ResponseStructure<BookResponseDto>> getBookByName(String bookName)
    {
        Book book=getBook(bookName);
        return bookMapper.mapToSuccessFetchBook(book);
    }


    @Override
    public ResponseEntity<ResponseStructure<BookResponseDto>> getBookById(Long bookId)
    {
        Book book=getBookByIdFromOptional(bookId);
        return bookMapper.mapToSuccessFetchBook(book);
    }


    @Override
    public ResponseEntity<ResponseStructure<List<BookResponseDto>>> getAllBooks()
    {
        List<Book> books=bookRepository.findAll();
        if(books.isEmpty())
            return bookMapper.noContent();

        List<BookResponseDto> bookResponseDtos =books.stream().map(bookMapper::mapBookToBookResponse).toList();
        return bookMapper.mapToSuccessGetAllBooks("Books fetched successfully",bookResponseDtos);
    }


    @Override
    public ResponseEntity<ResponseStructure<BookResponseDto>> updateBook(Long bookId, BookRequestDto bookRequestDTO)
    {
        Book book=getBookByIdFromOptional(bookId);
        Book updatedBook=bookMapper.updateCurrentBook(bookId,bookRequestDTO,book.getCartBookQuantity());
        Book saveUpdatedBook=bookRepository.save(updatedBook);
        return bookMapper.mapToSuccessUpdateBook(saveUpdatedBook);
    }


    @Override
    public ResponseEntity<ResponseStructure<String>> deleteBook(Long bookId)
    {
        Book book=getBookByIdFromOptional(bookId);
        bookRepository.delete(book);
        return bookMapper.mapToSuccessDeleteBook("Book with name "+book.getBookName()+" deleted successfully");
    }


    @Override
    public ResponseEntity<ResponseStructure<List<BookResponseDto>>> sortByBookName()
    {
        List<Book> books=bookRepository.findAll();
        if(books.isEmpty())
            return bookMapper.noContent();
        List<Book> sortedBooks=books.stream().sorted(Comparator.comparing(Book::getBookName)).toList();
        List<BookResponseDto> responseDTOs=
                sortedBooks.stream().map(book->new BookResponseDto(book.getBookId(),book.getBookName(),book.getBookAuthor(),book.getBookDescription(),book.getBookPrice(),book.getBookLogo())).toList();
        return bookMapper.mapToSuccessGetAllBooks("Books sorted successfully",responseDTOs);
    }

    @Override
    public ResponseEntity<ResponseStructure<List<BookResponseDto>>> sortByBookPrice()
    {
        List<Book> books=bookRepository.findAll();
        if(books.isEmpty())
            return bookMapper.noContent();
        List<Book> sortedBooks=books.stream().sorted(Comparator.comparing(Book::getBookPrice)).toList();
        List<BookResponseDto> responseDTOs=
                sortedBooks.stream().map(book->new BookResponseDto(book.getBookId(),book.getBookName(),book.getBookAuthor(),book.getBookDescription(),book.getBookPrice(),book.getBookLogo())).toList();
        return bookMapper.mapToSuccessGetAllBooks("Books sorted successfully",responseDTOs);
    }


    //Helper Methods
    private Book getBook(String bookName)
    {
        Optional<Book> book=bookRepository.findByBookName(bookName);
        if(book.isEmpty())
            throw new BookNotFoundException("Book with name "+bookName+" not found");
        return book.get();
    }

    private Book getBookByIdFromOptional(Long bookId)
    {
        Optional<Book> book=bookRepository.findById(bookId);
        if(book.isEmpty())
            throw new BookNotFoundException("Book with id "+bookId+" not found");
        return book.get();
    }
}
