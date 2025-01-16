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

import static org.junit.jupiter.api.Assertions.assertEquals;


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

        try {
            ResponseEntity<ResponseStructure<BookResponseDto>> response = restTemplate.exchange(baseUrl + "/addBook", HttpMethod.POST, entity,
                    new ParameterizedTypeReference<ResponseStructure<BookResponseDto>>() {});
        }
        catch (HttpClientErrorException exception) {
            assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
            exception.printStackTrace();
        }
    }

    @Test
    public void addBookTest_IfTokenIsInvalidOrMissing()
    {
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

        try {
            ResponseEntity<ResponseStructure<BookResponseDto>> response = restTemplate.exchange(baseUrl + "/addBook", HttpMethod.POST, entity,
                    new ParameterizedTypeReference<ResponseStructure<BookResponseDto>>() {});
        }
        catch (HttpClientErrorException exception) {
            assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
            exception.printStackTrace();
        }
    }


    @Test
    public void getBookByName_ValidScene()
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


        HttpEntity<Object> getEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<ResponseStructure<BookResponseDto>> getResponse = restTemplate.exchange(baseUrl + "/getBookByName/TEST", HttpMethod.GET, getEntity,
                new ParameterizedTypeReference<ResponseStructure<BookResponseDto>>() {});

        assertEquals(HttpStatus.OK,getResponse.getStatusCode());
        assertEquals(HttpStatus.OK.value(),getResponse.getBody().getStatus());
        assertEquals(bookRequestDto.getBookId(),getResponse.getBody().getData().getBookId());
        assertEquals(bookRequestDto.getBookName(),getResponse.getBody().getData().getBookName());
        assertEquals(1,bookH2Repository.findAll().size());
    }

    @Test
    public void getBookByName_IfWrongBookNameGiven()
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


        try {
            HttpEntity<Object> getEntity = new HttpEntity<>(httpHeaders);
            ResponseEntity<ResponseStructure<BookResponseDto>> getResponse = restTemplate.exchange(baseUrl + "/getBookByName/TESTING", HttpMethod.GET, getEntity,
                    new ParameterizedTypeReference<ResponseStructure<BookResponseDto>>() {});
        }
        catch (HttpClientErrorException exception) {
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
            exception.printStackTrace();
        }
    }


    @Test
    public void getBookById_ValidScene()
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


        HttpEntity<Object> getEntity = new HttpEntity<>(httpHeaders);
        ResponseEntity<ResponseStructure<BookResponseDto>> getResponse = restTemplate.exchange(baseUrl + "/getBookById/3245", HttpMethod.GET, getEntity,
                new ParameterizedTypeReference<ResponseStructure<BookResponseDto>>() {});

        assertEquals(HttpStatus.OK,getResponse.getStatusCode());
        assertEquals(HttpStatus.OK.value(),getResponse.getBody().getStatus());
        assertEquals(bookRequestDto.getBookId(),getResponse.getBody().getData().getBookId());
        assertEquals(bookRequestDto.getBookName(),getResponse.getBody().getData().getBookName());
        assertEquals(1,bookH2Repository.findAll().size());
    }


    @Test
    public void getBookById_IfBookIdNotFound()
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


        try {
            HttpEntity<Object> getEntity = new HttpEntity<>(httpHeaders);
            ResponseEntity<ResponseStructure<BookResponseDto>> getResponse = restTemplate.exchange(baseUrl + "/getBookById/3245", HttpMethod.GET, getEntity,
                    new ParameterizedTypeReference<ResponseStructure<BookResponseDto>>() {});
        }
        catch (HttpClientErrorException exception) {
            assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        }
    }
}
