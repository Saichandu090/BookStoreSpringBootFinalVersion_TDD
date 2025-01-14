package com.example.demo.serviceimpl;

import com.example.demo.entity.Book;
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
        if(bookRequestDTO==null)
        {
            throw new IllegalArgumentException("Book Should not be empty");
        }

        Book book=bookMapper.addBook(bookRequestDTO);

        Book savedBook=bookRepository.save(book);

        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseStructure<BookResponseDto>().
                setStatus(HttpStatus.CREATED.value())
                .setMessage("Book with name "+savedBook.getBookName()+" added successfully")
                .setData(bookMapper.mapBookToBookResponse(savedBook)));
    }

    //============================================================//

    @Override
    public ResponseEntity<ResponseStructure<BookResponseDto>> getBookByName(String bookName)
    {
        Optional<Book> book=bookRepository.findByBookName(bookName);

        if(book.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseStructure<BookResponseDto>()
                    .setStatus(HttpStatus.NOT_FOUND.value())
                    .setData(null)
                    .setMessage("Book not Found"));

        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<BookResponseDto>()
                .setStatus(HttpStatus.OK.value())
                .setMessage("Book fetched successfully")
                .setData(bookMapper.mapBookToBookResponse(book.get())));
    }


    //============================================================//


    @Override
    public ResponseEntity<ResponseStructure<BookResponseDto>> getBookById(Long bookId)
    {
        Optional<Book> book=bookRepository.findById(bookId);

        if(book.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseStructure<BookResponseDto>()
                    .setStatus(HttpStatus.NOT_FOUND.value())
                    .setData(null)
                    .setMessage("Book not Found"));

        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<BookResponseDto>()
                .setStatus(HttpStatus.OK.value())
                .setMessage("Book fetched successfully")
                .setData(bookMapper.mapBookToBookResponse(book.get())));
    }

    //============================================================//

    @Override
    public ResponseEntity<ResponseStructure<List<BookResponseDto>>> getAllBooks()
    {
        List<Book> books=bookRepository.findAll();

        if(books.isEmpty())
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ResponseStructure<List<BookResponseDto>>()
                    .setStatus(HttpStatus.NO_CONTENT.value())
                    .setMessage("Books are empty")
                    .setData(null));

        List<BookResponseDto> bookResponseDtos =books.stream().map(bookMapper::mapBookToBookResponse).toList();

        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<List<BookResponseDto>>()
                .setStatus(HttpStatus.OK.value())
                .setMessage("Books fetched successfully")
                .setData(bookResponseDtos));
    }


    //============================================================//

    @Override
    public ResponseEntity<ResponseStructure<BookResponseDto>> updateBook(Long bookId, BookRequestDto bookRequestDTO)
    {
        Optional<Book> book=bookRepository.findById(bookId);

        if(book.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseStructure<BookResponseDto>()
                    .setStatus(HttpStatus.NOT_FOUND.value())
                    .setData(null)
                    .setMessage("Book not Found"));

        Book updatedBook=bookMapper.updateCurrentBook(bookId,bookRequestDTO,book.get().getCartBookQuantity());
        Book saveUpdatedBook=bookRepository.save(updatedBook);

        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<BookResponseDto>()
                .setStatus(HttpStatus.OK.value())
                .setMessage("Book Updated successfully")
                .setData(bookMapper.mapBookToBookResponse(saveUpdatedBook)));
    }

    //============================================================//

    @Override
    public ResponseEntity<ResponseStructure<String>> deleteBook(Long bookId)
    {
        Optional<Book> book=bookRepository.findById(bookId);

        if(book.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseStructure<String>()
                    .setStatus(HttpStatus.NOT_FOUND.value())
                    .setData("Failure")
                    .setMessage("Book not Found"));

        bookRepository.delete(book.get());

        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<String>()
                .setStatus(HttpStatus.OK.value())
                .setMessage("Book with name "+book.get().getBookName()+" deleted successfully")
                .setData("Success"));
    }

    @Override
    public ResponseEntity<ResponseStructure<List<BookResponseDto>>> sortByBookName()
    {
        List<Book> books=bookRepository.findAll();

        if(books.isEmpty())
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ResponseStructure<List<BookResponseDto>>()
                    .setStatus(HttpStatus.NO_CONTENT.value())
                    .setMessage("Books are empty")
                    .setData(null));

        List<Book> sortedBooks=books.stream().sorted(Comparator.comparing(Book::getBookName)).toList();
        List<BookResponseDto> responseDTOs=
                sortedBooks.stream().map(book->new BookResponseDto(book.getBookId(),book.getBookName(),book.getBookAuthor(),book.getBookDescription(),book.getBookPrice(),book.getBookLogo())).toList();

        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<List<BookResponseDto>>()
                .setStatus(HttpStatus.OK.value())
                .setMessage("Books sorted successfully")
                .setData(responseDTOs));
    }

    @Override
    public ResponseEntity<ResponseStructure<List<BookResponseDto>>> sortByBookPrice()
    {
        List<Book> books=bookRepository.findAll();

        if(books.isEmpty())
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new ResponseStructure<List<BookResponseDto>>()
                    .setStatus(HttpStatus.NO_CONTENT.value())
                    .setMessage("Books are empty")
                    .setData(null));

        List<Book> sortedBooks=books.stream().sorted(Comparator.comparing(Book::getBookPrice)).toList();
        List<BookResponseDto> responseDTOs=
                sortedBooks.stream().map(book->new BookResponseDto(book.getBookId(),book.getBookName(),book.getBookAuthor(),book.getBookDescription(),book.getBookPrice(),book.getBookLogo())).toList();

        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<List<BookResponseDto>>()
                .setStatus(HttpStatus.OK.value())
                .setMessage("Books sorted successfully")
                .setData(responseDTOs));
    }
}
