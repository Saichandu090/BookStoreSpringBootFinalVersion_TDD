package com.example.demo.integrationtest;

import com.example.demo.integrationtest.repo.BookH2Repository;
import com.example.demo.requestdto.BookRequestDto;
import com.example.demo.requestdto.UserLoginDTO;
import com.example.demo.requestdto.UserRegisterDTO;
import com.example.demo.responsedto.BookResponseDto;
import com.example.demo.responsedto.LoginResponseDto;
import com.example.demo.responsedto.RegisterResponseDto;
import com.example.demo.util.ResponseStructure;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class BookControllerIT
{
    @LocalServerPort
    private int port;

    private String authToken;

    private String baseUrl="http://localhost";

    private static RestTemplate restTemplate;

    @Autowired
    private BookH2Repository bookH2Repository;

    @BeforeAll
    public static void init()
    {
        restTemplate=new RestTemplate();
    }

    @BeforeEach
    public void setUp()
    {
        baseUrl=baseUrl.concat(":").concat(port+"").concat("/book");
    }

    protected String getAuthToken()
    {
        if (authToken == null)
        {
            UserRegisterDTO userRegisterDTO=UserRegisterDTO.builder()
                    .firstName("Test")
                    .lastName("Chandu")
                    .dob(LocalDate.of(2002,8,24))
                    .email("test@gmail.com")
                    .role("ADMIN")
                    .password("saichandu@090").build();

            ResponseEntity<ResponseStructure<RegisterResponseDto>> registerResponse = restTemplate.exchange( "http://localhost:"+port+"/register", HttpMethod.POST, new HttpEntity<>(userRegisterDTO), new ParameterizedTypeReference<ResponseStructure<RegisterResponseDto>>(){});

            assertEquals(HttpStatus.CREATED,registerResponse.getStatusCode());
            assertEquals(HttpStatus.CREATED.value(),registerResponse.getBody().getStatus());

            UserLoginDTO userLoginDTO=UserLoginDTO.builder()
                    .email("test@gmail.com")
                    .password("saichandu@090").build();

            ResponseEntity<ResponseStructure<LoginResponseDto>> loginResponse = restTemplate.exchange(  "http://localhost:"+port+"/login", HttpMethod.POST, new HttpEntity<>(userLoginDTO), new ParameterizedTypeReference<ResponseStructure<LoginResponseDto>>(){});

            assertEquals(HttpStatus.OK,loginResponse.getStatusCode());
            assertEquals(HttpStatus.OK.value(),loginResponse.getBody().getStatus());
            assertEquals("test@gmail.com",loginResponse.getBody().getData().getEmail());
            assertEquals("ADMIN",loginResponse.getBody().getData().getRole());
            authToken = loginResponse.getBody().getMessage();
        }
        return authToken;
    }


    @Test
    public void addBookTest_ValidScene()
    {
        authToken=getAuthToken();

        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        BookRequestDto bookRequestDto=BookRequestDto.builder()
                .bookId(3245L)
                .bookName("TEST")
                .bookPrice(199.3)
                .bookAuthor("Chandu")
                .bookDescription("Atom")
                .bookQuantity(78)
                .bookLogo("URL").build();

        HttpEntity<Object> entity = new HttpEntity<>(bookRequestDto,httpHeaders);

        ResponseEntity<ResponseStructure<BookResponseDto>> response = restTemplate.exchange(baseUrl + "/addBook", HttpMethod.POST, entity,
                new ParameterizedTypeReference<ResponseStructure<BookResponseDto>>() {});

        assertEquals(HttpStatus.CREATED,response.getStatusCode());
        assertEquals(HttpStatus.CREATED.value(),response.getBody().getStatus());
        assertEquals(bookRequestDto.getBookId(),response.getBody().getData().getBookId());
        assertEquals(bookRequestDto.getBookName(),response.getBody().getData().getBookName());
    }

    @Test
    public void addBookTest_IfBodyIsInvalid()
    {
        authToken=getAuthToken();

        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        BookRequestDto bookRequestDto=BookRequestDto.builder()
                .bookId(3245L)
                .bookPrice(199.3)
                .bookAuthor("Chandu")
                .bookDescription("Atom")
                .bookQuantity(78)
                .bookLogo("URL").build();

        HttpEntity<Object> entity = new HttpEntity<>(bookRequestDto,httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()-> restTemplate.exchange(baseUrl + "/addBook", HttpMethod.POST, entity,
                new ParameterizedTypeReference<ResponseStructure<BookResponseDto>>() {}));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }


    @Test
    public void addBookTest_IfBookQuantityIsLessThan16()
    {
        authToken=getAuthToken();

        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        BookRequestDto bookRequestDto=BookRequestDto.builder()
                .bookName("Something")
                .bookId(3245L)
                .bookPrice(199.3)
                .bookAuthor("Chandu")
                .bookDescription("Atom")
                .bookQuantity(8)
                .bookLogo("URL").build();

        HttpEntity<Object> entity = new HttpEntity<>(bookRequestDto,httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()-> restTemplate.exchange(baseUrl + "/addBook", HttpMethod.POST, entity,
                new ParameterizedTypeReference<ResponseStructure<BookResponseDto>>() {}));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode(),"If ADMIN tries to add a book with less than 16 quantity");
    }

    @Test
    public void addBookTest_IfTokenIsInvalidOrMissing()
    {
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization",authToken);

        BookRequestDto bookRequestDto=BookRequestDto.builder()
                .bookId(3245L)
                .bookPrice(199.3)
                .bookAuthor("Chandu")
                .bookDescription("Atom")
                .bookQuantity(78)
                .bookLogo("URL").build();

        HttpEntity<Object> entity = new HttpEntity<>(bookRequestDto,httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/addBook", HttpMethod.POST, entity,
                new ParameterizedTypeReference<ResponseStructure<BookResponseDto>>() {}));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }


    @Test
    public void getBookByName_ValidScene()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        addBookTest_ValidScene();

        HttpEntity<Object> getEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<ResponseStructure<BookResponseDto>> getResponse = restTemplate.exchange(baseUrl + "/getBookByName/TEST", HttpMethod.GET, getEntity,
                new ParameterizedTypeReference<ResponseStructure<BookResponseDto>>() {});

        assertEquals(HttpStatus.OK,getResponse.getStatusCode());
        assertEquals(HttpStatus.OK.value(),getResponse.getBody().getStatus());
        assertEquals(3245,getResponse.getBody().getData().getBookId());
        assertEquals("TEST",getResponse.getBody().getData().getBookName());
        assertEquals(1,bookH2Repository.findAll().size());
    }

    @Test
    public void getBookByName_IfWrongBookNameGiven()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        addBookTest_ValidScene();
        HttpEntity<Object> getEntity = new HttpEntity<>(httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/getBookByName/TESTING", HttpMethod.GET, getEntity,
                new ParameterizedTypeReference<ResponseStructure<BookResponseDto>>() {}));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }


    @Test
    public void getBookById_ValidScene()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        addBookTest_ValidScene();

        HttpEntity<Object> getEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<ResponseStructure<BookResponseDto>> getResponse = restTemplate.exchange(baseUrl + "/getBookById/3245", HttpMethod.GET, getEntity,
                new ParameterizedTypeReference<ResponseStructure<BookResponseDto>>() {});

        assertEquals(HttpStatus.OK,getResponse.getStatusCode());
        assertEquals(HttpStatus.OK.value(),getResponse.getBody().getStatus());
        assertEquals(3245,getResponse.getBody().getData().getBookId());
        assertEquals("TEST",getResponse.getBody().getData().getBookName());
        assertEquals(1,bookH2Repository.findAll().size());
    }


    @Test
    public void getBookById_IfBookIdNotFound()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        addBookTest_ValidScene();

        HttpEntity<Object> getEntity = new HttpEntity<>(httpHeaders);
        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/getBookById/32451", HttpMethod.GET, getEntity,
                new ParameterizedTypeReference<ResponseStructure<BookResponseDto>>() {}));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }


    @Test
    public void updateBook_ValidScene()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        addBookTest_ValidScene();

        BookRequestDto bookRequestDto=BookRequestDto.builder()
                .bookId(3245L)
                .bookName("TESTING")
                .bookPrice(399.3)
                .bookAuthor("Manual Test")
                .bookDescription("Atom Bom")
                .bookQuantity(145)
                .bookLogo("URL").build();

        HttpEntity<Object> entity = new HttpEntity<>(bookRequestDto,httpHeaders);
        ResponseEntity<ResponseStructure<BookResponseDto>> response = restTemplate.exchange(baseUrl + "/updateBook/3245", HttpMethod.PUT, entity,
                new ParameterizedTypeReference<ResponseStructure<BookResponseDto>>() {});

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());
        assertEquals(3245,response.getBody().getData().getBookId());
        assertEquals("TESTING",response.getBody().getData().getBookName());
        assertEquals(399.3,response.getBody().getData().getBookPrice());
        assertEquals("Manual Test",response.getBody().getData().getBookAuthor());
    }


    @Test
    public void updateBook_IfBookIdIsInvalid()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        addBookTest_ValidScene();
        BookRequestDto bookRequestDto=BookRequestDto.builder()
                .bookId(32455L)
                .bookName("TESTING")
                .bookPrice(399.3)
                .bookAuthor("Manual Test")
                .bookDescription("Atom Bom")
                .bookQuantity(145)
                .bookLogo("URL").build();

        HttpEntity<Object> entity = new HttpEntity<>(bookRequestDto,httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/updateBook/32455", HttpMethod.PUT, entity,
                new ParameterizedTypeReference<ResponseStructure<BookResponseDto>>() {
                }));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }


    @Test
    public void updateBook_IfBookBodyIsNotValid()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        addBookTest_ValidScene();
        BookRequestDto bookRequestDto=BookRequestDto.builder()
                .bookPrice(399.3)
                .bookAuthor("Manual Test")
                .bookDescription("Atom Bom")
                .bookQuantity(145)
                .bookLogo("URL").build();

        HttpEntity<Object> entity = new HttpEntity<>(bookRequestDto,httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/updateBook/3245", HttpMethod.PUT, entity,
                new ParameterizedTypeReference<ResponseStructure<BookResponseDto>>() {
                }));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }


    @Test
    public void updateBook_IfTokenIsNotValid()
    {
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization",authToken);

        addBookTest_ValidScene();
        BookRequestDto bookRequestDto=BookRequestDto.builder()
                .bookName("Testing")
                .bookPrice(399.3)
                .bookAuthor("Manual Test")
                .bookDescription("Atom Bom")
                .bookQuantity(145)
                .bookLogo("URL").build();

        HttpEntity<Object> entity = new HttpEntity<>(bookRequestDto,httpHeaders);

        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> restTemplate.exchange(baseUrl + "/updateBook/3245", HttpMethod.PUT, entity,
                new ParameterizedTypeReference<ResponseStructure<BookResponseDto>>() {
                }));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }


    @Test
    public void deleteBook_ValidScene()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        addBookTest_ValidScene();

        HttpEntity<Object> entity = new HttpEntity<>(httpHeaders);
        ResponseEntity<ResponseStructure<String>> response = restTemplate.exchange(baseUrl + "/deleteBook/3245", HttpMethod.DELETE, entity,
                new ParameterizedTypeReference<ResponseStructure<String>>() {});

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());
        assertEquals(0,bookH2Repository.findAll().size());
    }


    @Test
    public void deleteBook_IfBookIdIsInvalid()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        addBookTest_ValidScene();

        HttpEntity<Object> entity = new HttpEntity<>(httpHeaders);

        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.exchange(baseUrl + "/deleteBook/" + 1234, HttpMethod.DELETE, entity,
                    new ParameterizedTypeReference<ResponseStructure<String>>() {});
        });
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }


    @Test
    public void deleteBook_IfTokenIsInvalid()
    {
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization",authToken);

        addBookTest_ValidScene();

        HttpEntity<Object> entity = new HttpEntity<>(httpHeaders);

        HttpClientErrorException exception = assertThrows(HttpClientErrorException.class, () -> {
            restTemplate.exchange(baseUrl + "/deleteBook/" + 3245, HttpMethod.DELETE, entity,
                    new ParameterizedTypeReference<ResponseStructure<String>>() {});
        });
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }


    public void addBookTest_ValidScene_SecondBook()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        BookRequestDto bookRequestDto=BookRequestDto.builder()
                .bookId(324L)
                .bookName("REST")
                .bookPrice(899.3)
                .bookAuthor("Sai")
                .bookDescription("Atom")
                .bookQuantity(708)
                .bookLogo("URL").build();

        HttpEntity<Object> entity = new HttpEntity<>(bookRequestDto,httpHeaders);
        ResponseEntity<ResponseStructure<BookResponseDto>> response = restTemplate.exchange(baseUrl + "/addBook", HttpMethod.POST, entity,
                new ParameterizedTypeReference<ResponseStructure<BookResponseDto>>() {});

        assertEquals(HttpStatus.CREATED,response.getStatusCode());
        assertEquals(HttpStatus.CREATED.value(),response.getBody().getStatus());
        assertEquals(bookRequestDto.getBookId(),response.getBody().getData().getBookId());
        assertEquals(bookRequestDto.getBookName(),response.getBody().getData().getBookName());
    }


    @Test
    public void getAllBooksTest()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        addBookTest_ValidScene();
        addBookTest_ValidScene_SecondBook();

        ResponseEntity<ResponseStructure<List<BookResponseDto>>> response = restTemplate.exchange(baseUrl + "/getBooks", HttpMethod.GET, new HttpEntity<>(httpHeaders),
                new ParameterizedTypeReference<ResponseStructure<List<BookResponseDto>>>() {});

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());
        assertEquals(2,bookH2Repository.findAll().size());
        assertEquals(2,response.getBody().getData().size());
        assertEquals(3245,response.getBody().getData().get(1).getBookId());
        assertEquals(324,response.getBody().getData().getFirst().getBookId());
    }

    @Test
    public void getAllBooksTest_IfTokenIsInvalid()
    {
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization",authToken);

        addBookTest_ValidScene();
        addBookTest_ValidScene_SecondBook();

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/getBooks", HttpMethod.GET, new HttpEntity<>(httpHeaders),
                new ParameterizedTypeReference<ResponseStructure<List<BookResponseDto>>>() {}));
        assertEquals(HttpStatus.UNAUTHORIZED,exception.getStatusCode());
    }


    @Test
    public void sortByBookNameTest_ValidScene()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        addBookTest_ValidScene();
        addBookTest_ValidScene_SecondBook();

        ResponseEntity<ResponseStructure<List<BookResponseDto>>> response = restTemplate.exchange(baseUrl + "/sortByBookName", HttpMethod.GET, new HttpEntity<>(httpHeaders),
                new ParameterizedTypeReference<ResponseStructure<List<BookResponseDto>>>() {});

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());
        assertEquals(2,bookH2Repository.findAll().size());
        assertEquals(2,response.getBody().getData().size());
        assertEquals("REST",response.getBody().getData().getFirst().getBookName());
        assertEquals("TEST",response.getBody().getData().get(1).getBookName());
    }


    @Test
    public void sortByBookPriceTest_ValidScene()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        addBookTest_ValidScene();
        addBookTest_ValidScene_SecondBook();

        ResponseEntity<ResponseStructure<List<BookResponseDto>>> response = restTemplate.exchange(baseUrl + "/sortByBookPrice", HttpMethod.GET, new HttpEntity<>(httpHeaders),
                new ParameterizedTypeReference<ResponseStructure<List<BookResponseDto>>>() {});

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());
        assertEquals(2,bookH2Repository.findAll().size());
        assertEquals(2,response.getBody().getData().size());
        assertEquals(199.3,response.getBody().getData().getFirst().getBookPrice());
        assertEquals(899.3,response.getBody().getData().get(1).getBookPrice());
    }
}
