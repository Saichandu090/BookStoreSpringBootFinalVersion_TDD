package com.example.bookstore.service;

import com.example.bookstore.entity.Book;
import com.example.bookstore.exception.*;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.requestdto.BookRequest;
import com.example.bookstore.responsedto.BookResponse;
import com.example.bookstore.serviceimpl.BookServiceImpl;
import com.example.bookstore.util.ResponseStructure;
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

    private Book book;
    private BookRequest bookRequest;
    private BookResponse bookResponse;

    @BeforeEach
    public void init()
    {
        bookRequest = BookRequest.builder()
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
                .status(true)
                .build();

        bookResponse = BookResponse.builder()
                .bookId(book.getBookId())
                .bookLogo(bookRequest.getBookLogo())
                .bookName(bookRequest.getBookName())
                .bookAuthor(bookRequest.getBookAuthor())
                .bookDescription(bookRequest.getBookDescription())
                .bookPrice(bookRequest.getBookPrice())
                .build();
    }



    @Test
    void addBookMustReturnCreatedStatus()
    {
        when(bookRepository.save(Mockito.any(Book.class))).thenReturn(book);
        when(bookRepository.existsByBookName(anyString())).thenReturn(false);

        ResponseEntity<ResponseStructure<BookResponse>> response=bookService.addBook(bookRequest);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Assertions.assertThat(response.getBody().getData().getBookId()).isEqualTo(bookResponse.getBookId());
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.CREATED.value());
        Assertions.assertThat(response.getBody().getMessage()).isEqualTo(response.getBody().getMessage());
    }


    @Test
    void addBookUrlLengthExceptionTest()
    {
        BookRequest book1=BookRequest.builder().bookLogo("""
                data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wCEAAkGBxMTEhITExMVFhUXGBYXGBgYGBcXGhsaGBgXGRgVFxYYHyghGBolHxcXIjEhJSkrLi4uFx8zODMtNygtLisBCgoKDg0OGxAQGismICY3Li0tKystLS4tLS0tNy8tKy0tLS0tLS0tLS0tLS0tKy0tKy0tLS0tLy0tKy0tLS0tL//AABEIAOEA4QMBIgACEQEDEQH/xAAcAAEAAgMBAQEAAAAAAAAAAAAABQYDBAcCAQj/xABHEAABAwI
                DBQQFCAgFAwUAAAABAAIRAyEEEjEFBkFRYRMicYEHMnKRoSMzQlKxwdHwFGJ0krKzwuEINFOCoiRDgxUXJVRz/8QAGwEBAAIDAQEAAAAAAAAAAAAAAAIDAQQFBgf/xAAzEQACAQIEAwYEBQUAAAAAAAAAAQIDEQQSITFBUWEFE3GBsfAiMpHRBiMzocEUYoKy4f/aAAwDAQACEQMRAD8A7iiIgCIiAIiIAiIgCIiAIiIAiIgCIiAIiIAiIgCIiAIiIAiIgCIiAIiIAiIgCItHa+16OGZ2leoKbZygmTJgmABJJgE25
                IDeRc92h6X8Az5ptasf1WZB76hafgqztD0z1zIo4amzq9zqh9zcse8q5UJvgYujtC8VarWiXODQOJIA95X51x/pE2jVmcS5gP0abW048HAZv+SgK+OfUcHVXuqHm9znn3uJVscI+LIuZ+jMbvtgKU5sTTcRwpk1D4dyVXcf6V8O2RSoVah5uy02nzufguMjEGJiwWzSr271vE/cVYsNBbmMzOkf+5mIq/N06dMdZc4eZIHwWvX3ixbyHGvUBB+iQ0e5sAjxVBbULe80kTxkgH3aqQw2IxYFhmaOL2iP3jE+9ZcaceS
                8ScX0OmbI34qtgV2CoPrNhrvNvqn/AIq4bM23Qr/NvGb6ps7906+S/P8AU25W4Fg8ANQLiTxXjD7UxGYHMHHUNbLnGOQpgx4mFCdCO+wzI/SiLle7u+tcHI5rzES2rBInhm9YefuV9wG36dQDNNN3J2n734wtWUHEyS6L40zcL6oAIiIAiIgCIiAIiIAiIgCIiAIiIAuY/wCINhOzqIGv6TT0n/Tq3te2vkunLnXpyLhgaBaJd+ksgHSTTrAcRGusiEQR+ejtIsIa/wCUGVhzeq4FzWkifpQSRfWNQt7D1mv9R09NH
                fu8fKVm2/gw+tTa31nG5y6gx3iJBmxMGSZFzNo3bGCqsNLMBlYGsDm5okuc8NObvMcM5s4NMN0teylimrJ8eBmULXN9fVhpOqZXOcMzWAEk2dBcGSHR3rkC8rJSeH+oZP1TZ3u4+UrfhWjLQpTTPRK3MEKdg9rbxlc4va3rIYJN+NlqU3lpkarJVqh5BNjxIvOvDWdLylROStrbmnqTjpqTdLazKds2YCQBSphgF+D3ku8wpDC4ug8B00g42Ae7M9p6mub/AO1jlVXUiBIu3iRBjzHBbezccWGGsBa4AOBJEkfSD9W
                H4cwVpzwkXG9Nu/jb67IsVV3tLYtOKaHUSKrqpH0XOP6PRB4Od25YHD2GknmtSlsf5IVcNVZiHZ2dyiHVC1zXAhxAsAImXgBRtGpRbUNU9s5x/wBSnh6kWtBqSJHA5RoLcFOYfabanybKOKxVXQZqjagbyNOi2m6kz2sh6EKCjVpKy234L7K3hbwMSakRdfZTqVZ3atqNnvgSGF03zC7he+kqR7kk0qlWGuLW5nPJIH0i0C0yPqixsTMbjtmva0OxDqeEzEh76+NFaqWgDudlTYM3DuyDz4KUobHwtNudz2V2xPaOq
                U6NEZpges6oT5DRSniYJK716Ws/4/cioskth7zupBrHOLTE3BIiYkg3Hmrrs3b7XgSWHq1wP/E3XIa2PwjTlo1mkanKJAdza4gF3QmYmy+0qTH3aT71hQclmaa8v44fUsutmd1p1mu0IK9ri2BljgQ97XDQiQfIyrns3eWswAVPlB17rveLHzHmsOHJ3MZWXZFG4DbdGrADsrvqusfLgfIqSVbViIREQBERAEREAREQBERAFzn06GMDQMgRiWXNx83V1HJdGXOvTkJwNC4H/Usv/wCOqpw+ZA4ttSgXGizLlY6wI7z
                Dn1ykDKYuZ1veCsu22ZqdA5QRLR3xBEAEZZh3OwJb4lZdtTmpMtlN8gA1Ga+aJg5rA9TF75doYZ5pU3g5qYILpAY7NIAc5urnQ+7gTPdJC1J/PBmKs2syXIzYfY9OrSIGYO7oJE3kzBBdlfciILXWHdJF4jZuxS9tSQZBAjjoVatmAFjhLWtgh3qgy4uj1jFwQJIJF7ttLdagMtXgCW6noV0IU3nkmc11vgi1yKricBWptzuGdmYNue/JaXC/KBx/FYKVRrrC55eq/wDArqe0NkUqlPMG5T3iPVExa4mDcG4I9krnG
                8eyuzymNSfgka7UW+XA3KLcpqHMwU5E5HeUQ73fgs9PHN0cyDrLReb6gm/D3KJZXcIBGfgJ9bwB18ltGrmlv0hILXiHgjUdY5fBWQqwqbb/AEf/AE2Z05Q3JfCsDr8NSQMxAPMERmiTE8DyK3DRqGwqOAgwJLQAQb5QQBaeF1WWPc0y0lp6EgrfbtZ7oD3O9ppLTpGmmt5jyVrTT2uVaGQbLyvY4szNBaXtHdzAOkszNkwRaYkSeUq3YLEsLXOZOHZI+SYRRbe4/wAvT7WvpfM9oVMcHZSWOa9s5i4xnERY5rxfQcQ
                eisWy6+drcrpgcDBB6SLXuq61ONaPv3cknlZu7Qodo5tWpTByiAauXD0tZ+b7z3z1cV5q0S1oeyGkfUp9lTcZtDqhl3u4LLh6L85ql4zwbkCTOsHnyIiF8OGv2gNR74ggFzesmpJc7+y0e6q02svDlt4cl46FuaMlqeDtt7TFWmARrl7pW9gt4mZspziTE90jxJuVo0g+fm29QZdPUk94+9DsYcRHHktyMc/zxs+nv7+JW3l+Vl0wtdjwIc13hH2KXwW0qlOzXSPqm4/t5LmjdmEEFhLSORUzgNoV2WfDx1s73rMqS
                4MyqnNHTsLt1hs8ZTz1H4hStOoHCWkEcxdc3pbSB1Dh4j7wpHDY0tux0Hp9/NUOmzLyvYvKKvYXeAi1QT1Fj7uPwVga6QDzuq2miJ9REWAEREAREQBc69OU/oNCP/ss/l1V0Vc69OH+SoftLP5dVWUfnQOMucXvaHQC0FromDlz3ygQNYtz6mJzbM5KcOMEiWgwCJBlwBOhjibkmZsoXDUz2w65j5QfwU1jXFwAdq063vJbw0nrx4qnFxSxELdDUr1LZk+RIbKw4LHmpGSHCYIcIAmHNBlnMEEdJ1z7vU2jt8plufu
                mNReDEcoXzZ7wKb+62wccvhpIglwubmQI4TKzbs04bU9oX8uq6UYvPJs5edZIpcixPg0nCRGgEhtxBy9Tble1+Co+9VGeyEcXf0/iugU6bez+UNjEGIdx1InMOUg+Wqpe8rb0uXenhPqW+C0qllSqW9++J1+zlmxdJPr6Mgth7HZWeW9p2bu4Gx2ZcSSJIa8tzAZZhpzCRAdcKI3g2U+nWfnLXOc5xlpBuTMFsBzHSbtc1pHK6tOwmgVM0E5RmDZbBkhpJBgEBhfNxa8iCvm+JLqjMxGXIHNBJc4B0k3juiW+qHOaA
                RBMkrlxqO56erR/MsihOxTmwCMw66jwKz0azXeqb8jY/gVvVdn5zoO60aWmT9vCenFMZsE0sPUe+ib9llqBzXBrpJcxzRdhykAgiQWjgVvUsc4uzZo1sEnqtzUK90XGbGFGjFPZZwzAWg6jwK2qGJY7QweRsfI6FdOlXjPVHNqUpQdmWPCvcCHA35qao7x1m6hhHhH2Kq4DGZDDgSFNUajKglpnpor3YqLNgN4GOkPGU9JIUrQxDHjukEKnUKEXMLbwWJbSdmkqDSMlvZSbrlHuX0U5OnwCj8HtI1PVbA5n8FvsaSq
                W0iaRu0msGlz+eK9FvJeKLIW1RZmIAVDmTUbnmnR81e2iFWMOxstHUD4q0KlzzEpxyhERYIBERAEREAXOPTm6MFh/2ln8qsujrmH+IIxgMObWxVM30+brc9FKEsskwcrwZmszjAdPj3vepzGs1PMj7Qq7u8czqdQaHMPA5ZI+Ks+MFh4/1BV4qSeIhbp6nMxm78PueS2GyJBJcNTpA5a6qZ3b9WoYtn0/2iyiwyW+GY/whS+7Q7lT2/6Wr0E0reZw6LeZeBPl/wAmRbgD06kRqI1PkVT96G96l7L/ALWK0uEfBVvef
                16Xsu+1q5GMgoYabXG3qj03YknPtCkn19GRWzPnGkNe4zMscG1AQJzMzWJEHWQdLStjezZlRjw9z87XE5T3wREFwyvnKMzz6rnCSRaIXvYpaKocSZAcZtDRa5sTeSPom4uJlYd5GDtnRPDNwAcRoALAQBa+mrtVwE9T29SF6vkYNg4eXVAQNG9NS7ieCtQons3FziTE+tcNyy1swSRciKmZlxYqA3ZZeqY+p9rlZmuIZMZo4gwQIiDxbbjytPBadWo1VZCpTTicp27sjLdoMF0C1+PAcVBtwIkFxgZmjiBc3GYA5TF
                9D4HRdB3sp/Js9sfwuWhu6+HBgFiSSQSDw7kQ5tSzZALZmYIuV3sFJvC57czkY2mu9sU5+LyPcGEvphxy5/WyzaSNDCs2wNoscw5WEEG99StPePZrGuGQNEySAXC9r5HyWzOgc4awRcD7u7Tytqe0I9y6NKbaTWxzalPKWCm99QhrdSdApPDbIcH9+I+3opLd7ZhpNL6kBxAIHEDryKzNqS/zWHVbegUNNTfwmHDQt1llgw5WwqnLUJGzRbK28O2O8fJYsIO71K2XXjoqJS1L4x0ufcESatMfrN+BBVvVa2S0CowH1
                iTHkCfuVlWUVVNwiIslYREQBERAFzf07f5KhByk4hoDuRNGsA7pBOq6Qub+nf8AyND9ob/KrKdNXkkYZzDdvAPpio2pBJyPa5twRFRpcZGvqT4jjKltpNLWF3gfiFX9kkU8SJJe0sLT3jEFp7s8gSfirRiC1tJlNoL6udrhcEOaHNgQfVkGCDxEhaeJThiY+RCdCNaEk3ql+2up4AIaJGoMHxi8+9TG7g7j/b/paq1iMPiaYbTDTUYyWy0ZnDN3gHtbdnECdYPQKxbsT2bw4EOz3BsRLGGCOd16XvIuPW55pYecJ9E
                    """).build();

        assertThrows(BookUrlLengthException.class,()->bookService.addBook(book1));
    }


    @Test
    void addBookIfBookAlreadyExists()
    {
        when(bookRepository.existsByBookName(anyString())).thenReturn(true);

        assertThrows(BookAlreadyExistsException.class,()->bookService.addBook(bookRequest));
    }



    @Test
    void getBookByNameMustReturnOKStatusCode()
    {
        when(bookRepository.findByBookName(Mockito.anyString())).thenReturn(Optional.of(book));

        ResponseEntity<ResponseStructure<BookResponse>> response=bookService.getBookByName(bookRequest.getBookName());

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody().getData().getBookId()).isEqualTo(bookResponse.getBookId());
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());
        Assertions.assertThat(response.getBody().getMessage()).isEqualTo(response.getBody().getMessage());
    }



    @Test
    void getBookByNameMustThrowBookNotFoundException()
    {
        when(bookRepository.findByBookName(Mockito.anyString())).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class,()->bookService.getBookByName(bookRequest.getBookName()));

        verify(bookRepository,times(1)).findByBookName(anyString());
    }



    @Test
    void getBookByIdMustReturnOKStatusCode()
    {
        when(bookRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(book));

        ResponseEntity<ResponseStructure<BookResponse>> response=bookService.getBookById(book.getBookId());

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody().getData().getBookId()).isEqualTo(bookResponse.getBookId());
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());
        Assertions.assertThat(response.getBody().getMessage()).isEqualTo(response.getBody().getMessage());
    }



    @Test
    void getBookByIdMustReturnNotFoundStatusCode()
    {
        when(bookRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class,()->bookService.getBookById(book.getBookId()));

        verify(bookRepository,times(1)).findById(anyLong());
    }


    @Test
    void getAllBooksMustReturnOKStatusCode()
    {
        when(bookRepository.findAll()).thenReturn(List.of(book));

        ResponseEntity<ResponseStructure<List<BookResponse>>> response=bookService.getAllBooks();

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());
        Assertions.assertThat(response.getBody().getData().size()).isEqualTo(1);
        Assertions.assertThat(response.getBody().getData().getFirst().getBookId()).isEqualTo(book.getBookId().intValue());
    }



    @Test
    void getAllBooksMustReturnNoContentStatusCode()
    {
        when(bookRepository.findAll()).thenReturn(List.of());

        ResponseEntity<ResponseStructure<List<BookResponse>>> response=bookService.getAllBooks();

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        Assertions.assertThat(Objects.requireNonNull(response.getBody()).getStatus()).isEqualTo(HttpStatus.NO_CONTENT.value());
    }



    @Test
    void updateBookMustReturnOKStatusCode()
    {
        when(bookRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(book));
        when(bookRepository.save(Mockito.any(Book.class))).thenReturn(book);

        ResponseEntity<ResponseStructure<BookResponse>> response=bookService.updateBook(book.getBookId(), bookRequest);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());
        Assertions.assertThat(response.getBody().getData().getBookName()).isEqualTo(book.getBookName());
        Assertions.assertThat(response.getBody().getData().getBookId()).isEqualTo(book.getBookId().intValue());
    }



    @Test
    void updateBookMustReturnNotFoundStatusCode()
    {
        when(bookRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class,()->bookService.updateBook(book.getBookId(), bookRequest));

        verify(bookRepository,times(1)).findById(anyLong());
    }



    @Test
    void deleteBookMustReturnOKStatusCode()
    {
        when(bookRepository.findById(Mockito.anyLong())).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        ResponseEntity<ResponseStructure<String>> response=bookService.deleteBook(book.getBookId());

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody().getStatus()).isEqualTo(HttpStatus.OK.value());
        Assertions.assertThat(response.getBody().getData()).isEqualTo("Success");

        Mockito.verify(bookRepository,times(1)).save(book);
    }


    @Test
    void deleteBookMustReturnNotFoundStatusCode()
    {
        when(bookRepository.findById(Mockito.anyLong())).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class,()->bookService.deleteBook(book.getBookId()));

        Mockito.verify(bookRepository,times(0)).delete(book);
    }


    @Test
    void findBooksWithSortingMustReturnOKStatusCodeBookPrice()
    {
        Book first=Book.builder()
                .bookId(2L)
                .bookLogo("My url")
                .bookName("Lawsuit")
                .bookAuthor("Carley")
                .bookDescription("Law")
                .bookPrice(220.99)
                .status(true)
                .build();

        Book second=Book.builder()
                .bookId(3L)
                .bookLogo("Man")
                .bookName("Jimmy man")
                .bookAuthor("Brother")
                .bookDescription("Family")
                .bookPrice(225.00)
                .status(true)
                .build();

        Book third=Book.builder()
                .bookId(4L)
                .bookLogo("URL")
                .bookName("James miggel")
                .bookAuthor("Chuck")
                .bookDescription("Descript")
                .bookPrice(99.90)
                .status(true)
                .build();

        List<Book> books=new ArrayList<>();
        books.add(third);
        books.add(first);
        books.add(second);

        when(bookRepository.findAll(Sort.by(Sort.Direction.ASC,"bookPrice"))).thenReturn(books);

        ResponseEntity<ResponseStructure<List<BookResponse>>> response=bookService.findBooksWithSorting("bookPrice");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(Objects.requireNonNull(response.getBody()).getStatus()).isEqualTo(HttpStatus.OK.value());
        Assertions.assertThat(response.getBody().getData().size()).isEqualTo(3);
        assertEquals(4,response.getBody().getData().getFirst().getBookId());
        assertEquals(2,response.getBody().getData().get(1).getBookId());
        assertEquals(3,response.getBody().getData().get(2).getBookId());
    }


    @Test
    void findBooksWithSortingIfFieldIsInvalid()
    {
        assertThrows(InvalidSortingFieldException.class,()->bookService.findBooksWithSorting("Test"));
    }



    @Test
    void paginationValidTest()
    {
        List<Book> books = IntStream.range(1, 20)
                .mapToObj(i -> Book.builder().bookId((long) i).status(true).build())
                .collect(Collectors.toList());

        Page<Book> bookPage = new PageImpl<>(books.subList(0,10), PageRequest.of(0, 10), books.size());
        when(bookRepository.findAll(PageRequest.of(0,10))).thenReturn(bookPage);

        ResponseEntity<ResponseStructure<List<BookResponse>>> response=bookService.findBooksWithPagination(0,10);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertEquals(10,response.getBody().getData().size(),"Ensuring the size of books per page provided");
        assertEquals(1L, response.getBody().getData().get(0).getBookId(),"Expecting that first page should start with 1");
        assertEquals(10L, response.getBody().getData().get(9).getBookId(),"Expecting that first page should end with 10");
    }


    @Test
    void paginationSecondPageTest()
    {
        List<Book> books = IntStream.rangeClosed(1, 20)
                .mapToObj(i -> Book.builder().bookId((long) i).status(true).build())
                .collect(Collectors.toList());

        Page<Book> bookPage = new PageImpl<>(books.subList(10, 20), PageRequest.of(1, 10), books.size());
        when(bookRepository.findAll(PageRequest.of(1, 10))).thenReturn(bookPage);

        ResponseEntity<ResponseStructure<List<BookResponse>>> response = bookService.findBooksWithPagination(1, 10);

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertEquals(10, response.getBody().getData().size());
        assertEquals(11L, response.getBody().getData().get(0).getBookId(),"Expecting that second page should start with 11");
        assertEquals(20L, response.getBody().getData().get(9).getBookId(),"Expecting that second page should end with 20");
    }


    @Test
    void paginationIfNoBooksToDisplay()
    {
        when(bookRepository.findAll(PageRequest.of(1, 10))).thenReturn(Page.empty());

        ResponseEntity<ResponseStructure<List<BookResponse>>> response = bookService.findBooksWithPagination(1, 10);

        assertEquals(HttpStatus.NO_CONTENT,response.getStatusCode());
    }

    @Test
    void paginationIfGivenInvalidPageNumber()
    {
        assertThrows(InvalidPaginationException.class,()->bookService.findBooksWithPagination(-1, 10));
    }


    @Test
    void searchQueryValidTest()
    {
        List<Book> books = IntStream.range(1, 20)
                .mapToObj(i -> Book.builder().bookId((long) i).status(true).build())
                .collect(Collectors.toList());

        when(bookRepository.searchBooksByKeyword(anyString())).thenReturn(books);
        ResponseEntity<ResponseStructure<List<BookResponse>>> response=bookService.searchBooks("anything");

        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertEquals("Books matching the query: anything",response.getBody().getMessage());
    }


    @Test
    void searchQueryIfNothingMatches()
    {
        when(bookRepository.searchBooksByKeyword(anyString())).thenReturn(List.of());
        ResponseEntity<ResponseStructure<List<BookResponse>>> response=bookService.searchBooks("anything");
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertEquals("No Books Available",response.getBody().getMessage());
    }

    @Test
    void searchQueryIfQueryIsEmpty()
    {
        ResponseEntity<ResponseStructure<List<BookResponse>>> response=bookService.searchBooks("");
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertEquals("No Books Available",response.getBody().getMessage());
    }
}