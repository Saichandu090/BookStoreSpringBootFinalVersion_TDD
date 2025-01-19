package com.example.demo.serviceimpl;

import com.example.demo.entity.Book;
import com.example.demo.exception.BookNotFoundException;
import com.example.demo.exception.InvalidPaginationException;
import com.example.demo.exception.InvalidSortingFieldException;
import com.example.demo.mapper.BookMapper;
import com.example.demo.requestdto.BookRequest;
import com.example.demo.repository.BookRepository;
import com.example.demo.responsedto.BookResponse;
import com.example.demo.service.BookService;
import com.example.demo.util.ResponseStructure;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class BookServiceImpl implements BookService
{
    private BookRepository bookRepository;
    private final BookMapper bookMapper=new BookMapper();

    @Override
    public ResponseEntity<ResponseStructure<BookResponse>> addBook(BookRequest bookRequest)
    {
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
        List<String> validFields = List.of("bookId", "bookName", "bookAuthor", "bookPrice");
        if (!validFields.contains(field))
            throw new InvalidSortingFieldException("The field '" + field + "' is not a valid sorting field. Valid fields are: "+ validFields);
        List<Book> sortedBooks=bookRepository.findAll(Sort.by(Sort.Direction.ASC,field));
        if(sortedBooks.isEmpty())
            return bookMapper.noContent();
        List<BookResponse> responseDTOs= sortedBooks.stream().map(bookMapper::mapBookToBookResponse).toList();
        return bookMapper.mapToSuccessGetAllBooks("Books sorted successfully based on "+field,responseDTOs);
    }


    @Override
    public ResponseEntity<ResponseStructure<List<BookResponse>>> searchBooks(String query)
    {
        if(query==null || query.trim().isEmpty())
            return bookMapper.noContent();
        List<Book> foundBooks = bookRepository.findByBookNameContainingOrBookAuthorContainingOrBookDescriptionContaining(query, query, query);
        if (foundBooks.isEmpty())
            return bookMapper.noContent();
        List<BookResponse> responseDTOs = foundBooks.stream()
                .map(bookMapper::mapBookToBookResponse)
                .collect(Collectors.toList());
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
        List<BookResponse> responseDTOs= bookList.stream().map(bookMapper::mapBookToBookResponse).toList();
        return bookMapper.mapToSuccessGetAllBooks("Books fetched successfully",responseDTOs);
    }


    @Override
    public ResponseEntity<ResponseStructure<List<BookResponse>>> getAllBooks()
    {
        List<Book> books=bookRepository.findAll();
        if(books.isEmpty())
            return bookMapper.noContent();
        List<BookResponse> bookResponse =books.stream().map(bookMapper::mapBookToBookResponse).toList();
        return bookMapper.mapToSuccessGetAllBooks("Books fetched successfully", bookResponse);
    }


    @Override
    public ResponseEntity<ResponseStructure<BookResponse>> updateBook(Long bookId, BookRequest bookRequest)
    {
        Book book=getBookByIdFromOptional(bookId);
        Book updatedBook=bookMapper.updateCurrentBook(bookId, bookRequest,book.getCartBookQuantity());
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
