package com.example.bookstore.serviceimpl;

import com.example.bookstore.entity.Book;
import com.example.bookstore.exception.BookAlreadyExistsException;
import com.example.bookstore.exception.BookNotFoundException;
import com.example.bookstore.exception.InvalidPaginationException;
import com.example.bookstore.exception.InvalidSortingFieldException;
import com.example.bookstore.mapper.BookMapper;
import com.example.bookstore.requestdto.BookRequest;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.responsedto.BookResponse;
import com.example.bookstore.service.BookService;
import com.example.bookstore.util.ResponseStructure;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class BookServiceImpl implements BookService
{
    private BookRepository bookRepository;
    private final BookMapper bookMapper=new BookMapper();

    @Override
    public ResponseEntity<ResponseStructure<BookResponse>> addBook(BookRequest bookRequest)
    {
        checkBook(bookRequest.getBookName());
        Book book=bookMapper.addBook(bookRequest);
        Book savedBook=bookRepository.save(book);
        return bookMapper.mapToSuccessAddBook(savedBook);
    }


    @Override
    public ResponseEntity<ResponseStructure<BookResponse>> getBookByName(String bookName)
    {
        Book book=getBook(bookName);
        return bookMapper.mapToSuccessFetchBook(book);
    }


    @Override
    public ResponseEntity<ResponseStructure<BookResponse>> getBookById(Long bookId)
    {
        Book book=getBookByIdFromOptional(bookId);
        return bookMapper.mapToSuccessFetchBook(book);
    }

    @Override
    public ResponseEntity<ResponseStructure<List<BookResponse>>> findBooksWithSorting(String field)
    {
        List<String> validFields = List.of("bookName", "bookAuthor", "bookPrice");
        if (!validFields.contains(field))
            throw new InvalidSortingFieldException("The field '" + field + "' is not a valid sorting field. Valid fields are: "+ validFields);
        List<Book> sortedBooks=bookRepository.findAll(Sort.by(Sort.Direction.ASC,field));
        if(sortedBooks.isEmpty())
            return bookMapper.noContent();
        List<Book> activeBooks=sortedBooks.parallelStream().filter(Book::getStatus).toList();
        List<BookResponse> responseDTOs= activeBooks.parallelStream().map(bookMapper::mapBookToBookResponse).toList();//Converting sorted books into list of BookResponse with stream and BookMapper
        return bookMapper.mapToSuccessGetAllBooks("Books sorted successfully based on "+field,responseDTOs);
    }


    @Override
    public ResponseEntity<ResponseStructure<List<BookResponse>>> searchBooks(String query)
    {
        if(query==null || query.trim().isEmpty())
            return bookMapper.noContent();
        List<Book> foundBooks = bookRepository.searchBooksByKeyword(query);
        if (foundBooks.isEmpty())
            return bookMapper.noContent();
        List<Book> activeBooks=foundBooks.parallelStream().filter(Book::getStatus).toList();
        List<BookResponse> responseDTOs = activeBooks.parallelStream().map(bookMapper::mapBookToBookResponse).toList();//Converting found books to list of bookResponse using stream and BookMapper
        return bookMapper.mapToSuccessGetAllBooks("Books matching the query: " + query, responseDTOs);
    }


    @Override
    public ResponseEntity<ResponseStructure<List<BookResponse>>> findBooksWithPagination(int pageNumber, int pageSize)
    {
        if(pageNumber<0 || pageSize<=0)
            throw new InvalidPaginationException("Page number must be non-negative and Page size must be greater than 0.");
        Page<Book> books=bookRepository.findAll(PageRequest.of(pageNumber,pageSize));
        List<Book> bookList=books.getContent();
        if(bookList.isEmpty())
            return bookMapper.noContent();
        List<Book> activeBooks=bookList.parallelStream().filter(Book::getStatus).toList();
        List<BookResponse> responseDTOs= activeBooks.stream().map(bookMapper::mapBookToBookResponse).toList();//Converting bookList into list of BookResponse using stream and BookMapper
        return bookMapper.mapToSuccessGetAllBooks("Books fetched successfully",responseDTOs);
    }


    @Override
    public ResponseEntity<ResponseStructure<List<BookResponse>>> getAllBooks()
    {
        List<Book> books=bookRepository.findAll();
        if(books.isEmpty())
            return bookMapper.noContent();
        List<Book> activeBooks=books.stream().filter(Book::getStatus).toList();
        List<BookResponse> bookResponse =activeBooks.parallelStream().map(bookMapper::mapBookToBookResponse).toList();//Converting books into List of BookResponse using Stream and BookMapper
        return bookMapper.mapToSuccessGetAllBooks("Books fetched successfully", bookResponse);
    }


    @Override
    public ResponseEntity<ResponseStructure<BookResponse>> updateBook(Long bookId, BookRequest bookRequest)
    {
        Book book=getBookByIdFromOptional(bookId);
        Book updatedBook=bookMapper.updateCurrentBook(book, bookRequest);
        Book saveUpdatedBook=bookRepository.save(updatedBook);
        return bookMapper.mapToSuccessUpdateBook(saveUpdatedBook);
    }


    @Override
    public ResponseEntity<ResponseStructure<String>> deleteBook(Long bookId)
    {
        Book book=getBookByIdFromOptional(bookId);
        if(Boolean.FALSE.equals(book.getStatus()))
            throw new BookNotFoundException("Book not found with Id "+bookId);
        book.setStatus(false);
        Book deletedBook=bookRepository.save(book);
        return bookMapper.mapToSuccessDeleteBook("Book with name "+deletedBook.getBookName()+" deleted successfully");
    }


    //Helper Methods
    private Book getBook(String bookName)
    {
        Optional<Book> book=bookRepository.findByBookName(bookName);
        if(book.isEmpty())
            throw new BookNotFoundException("Book with name "+bookName+" not found");
        return book.get();
    }

    private void checkBook(String bookName)
    {
        boolean isBookExist=bookRepository.existsByBookName(bookName);
        if(Boolean.TRUE.equals(isBookExist))
            throw new BookAlreadyExistsException("Book with name "+bookName+" already exists.");
    }

    private Book getBookByIdFromOptional(Long bookId)
    {
        Optional<Book> book=bookRepository.findById(bookId);
        if(book.isEmpty())
            throw new BookNotFoundException("Book with id "+bookId+" not found.");
        return book.get();
    }
}
