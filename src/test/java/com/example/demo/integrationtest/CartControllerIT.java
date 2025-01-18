package com.example.demo.integrationtest;

import com.example.demo.entity.Book;
import com.example.demo.entity.User;
import com.example.demo.integrationtest.repo.BookH2Repository;
import com.example.demo.integrationtest.repo.CartH2Repository;
import com.example.demo.integrationtest.repo.UserH2Repository;
import com.example.demo.requestdto.CartRequestDto;
import com.example.demo.requestdto.UserLoginDTO;
import com.example.demo.requestdto.UserRegisterDTO;
import com.example.demo.responsedto.CartResponseDto;
import com.example.demo.responsedto.LoginResponseDto;
import com.example.demo.responsedto.RegisterResponseDto;
import com.example.demo.service.CartService;
import com.example.demo.util.ResponseStructure;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CartControllerIT
{
    @LocalServerPort
    private int port;

    @Autowired
    private BookH2Repository bookH2Repository;

    @Autowired
    private UserH2Repository userH2Repository;

    @Autowired
    private CartH2Repository cartH2Repository;

    @Autowired
    private CartService cartService;

    private String baseUrl="http://localhost";

    private static RestTemplate restTemplate;

    private String authToken;

    @BeforeAll
    static void init()
    {
        restTemplate=new RestTemplate();
    }

    @BeforeEach
    void setUp()
    {
        baseUrl=baseUrl.concat(":").concat(port+"").concat("/cart");
    }

    @BeforeEach
    void addBooksToRepository()
    {
        Book book1=Book.builder()
                .bookId(1L)
                .bookName("TEST")
                .bookPrice(199.3)
                .bookAuthor("Chandu")
                .bookDescription("Atom")
                .bookQuantity(3)
                .bookLogo("URL").build();

        Book book2=Book.builder()
                .bookId(2L)
                .bookName("Habits")
                .bookPrice(249.49)
                .bookAuthor("Zak crawly")
                .bookDescription("Cricket")
                .bookQuantity(169)
                .bookLogo("URL").build();

        Book book3=Book.builder()
                .bookId(3L)
                .bookName("Gotye")
                .bookPrice(789.49)
                .bookAuthor("Ryan")
                .bookDescription("Deadpool")
                .bookQuantity(2)
                .bookLogo("URL").build();

        bookH2Repository.save(book1);
        bookH2Repository.save(book2);
        bookH2Repository.save(book3);
    }


    protected String getAuthToken()
    {
        if (authToken == null)
        {
            UserRegisterDTO userRegisterDTO=UserRegisterDTO.builder()
                    .firstName("Soul")
                    .lastName("Dinesh")
                    .dob(LocalDate.of(1992,8,24))
                    .email("dinesh@gmail.com")
                    .role("USER")
                    .password("dinesh@090").build();

            ResponseEntity<ResponseStructure<RegisterResponseDto>> registerResponse = restTemplate.exchange( "http://localhost:"+port+"/register", HttpMethod.POST, new HttpEntity<>(userRegisterDTO), new ParameterizedTypeReference<ResponseStructure<RegisterResponseDto>>(){});

            assertEquals(HttpStatus.CREATED,registerResponse.getStatusCode());
            assertEquals(HttpStatus.CREATED.value(),registerResponse.getBody().getStatus());

            UserLoginDTO userLoginDTO=UserLoginDTO.builder()
                    .email("dinesh@gmail.com")
                    .password("dinesh@090").build();

            ResponseEntity<ResponseStructure<LoginResponseDto>> loginResponse = restTemplate.exchange(  "http://localhost:"+port+"/login", HttpMethod.POST, new HttpEntity<>(userLoginDTO), new ParameterizedTypeReference<ResponseStructure<LoginResponseDto>>(){});

            assertEquals(HttpStatus.OK,loginResponse.getStatusCode());
            assertEquals(HttpStatus.OK.value(),loginResponse.getBody().getStatus());
            assertEquals("dinesh@gmail.com",loginResponse.getBody().getData().getEmail());
            assertEquals("USER",loginResponse.getBody().getData().getRole());
            authToken = loginResponse.getBody().getMessage();
        }
        return authToken;
    }


    @Test
    void addToCart_ValidTest_MultipleTimesUntilBookOutOfStock()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);
        CartRequestDto cartRequestDto=CartRequestDto.builder().bookId(3L).build();
        HttpEntity<Object> httpEntity=new HttpEntity<>(cartRequestDto,httpHeaders);

        ResponseEntity<ResponseStructure<CartResponseDto>> response=restTemplate.exchange(baseUrl + "/addToCart", HttpMethod.POST, httpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponseDto>>() {});

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());
        assertEquals(3,response.getBody().getData().getBookId());
        assertEquals(1,response.getBody().getData().getCartQuantity());

        Book book=bookH2Repository.findById(3L).get();
        assertEquals(1,book.getBookQuantity());

        User user=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(1,user.getCarts().size());


        CartRequestDto cartRequestDto2=CartRequestDto.builder().bookId(3L).build();
        HttpEntity<Object> httpEntity2=new HttpEntity<>(cartRequestDto2,httpHeaders);

        ResponseEntity<ResponseStructure<CartResponseDto>> response2=restTemplate.exchange(baseUrl + "/addToCart", HttpMethod.POST, httpEntity2, new ParameterizedTypeReference<ResponseStructure<CartResponseDto>>() {});

        assertEquals(HttpStatus.OK,response2.getStatusCode());
        Book book2=bookH2Repository.findById(3L).get();
        assertEquals(0,book2.getBookQuantity());

        User user2=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(1,user2.getCarts().size());


        CartRequestDto cartRequestDto3=CartRequestDto.builder().bookId(3L).build();
        HttpEntity<Object> httpEntity3=new HttpEntity<>(cartRequestDto3,httpHeaders);

        ResponseEntity<ResponseStructure<CartResponseDto>> response3=restTemplate.exchange(baseUrl + "/addToCart", HttpMethod.POST, httpEntity3, new ParameterizedTypeReference<ResponseStructure<CartResponseDto>>() {});
        assertEquals(HttpStatus.NO_CONTENT,response3.getStatusCode());

        Book book3=bookH2Repository.findById(3L).get();
        assertEquals(0,book3.getBookQuantity());
    }


    @Test
    void testAddToCartConcurrency_WhenTwoUsersTryToBuyTheLastBook() throws InterruptedException
    {
        Long bookId = 123L;
        CartRequestDto cartRequestDto1 = CartRequestDto.builder()
                .bookId(bookId)
                .build();

        User user1 = new User();
        user1.setEmail("user1@gmail.com");
        user1.setCarts(new ArrayList<>());
        userH2Repository.save(user1);

        User user2 = new User();
        user2.setEmail("user2@gmail.com");
        user2.setCarts(new ArrayList<>());
        userH2Repository.save(user2);

        Book book = new Book();
        book.setBookId(bookId);
        book.setBookQuantity(1);
        bookH2Repository.save(book);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(2);

        List<Future<ResponseEntity<ResponseStructure<CartResponseDto>>>> results = new ArrayList<>();

        Runnable task1 = () -> {
            try {
                results.add(CompletableFuture.completedFuture(cartService.addToCart(user1.getEmail(), cartRequestDto1)));
            } finally {
                latch.countDown();
            }
        };
        Runnable task2 = () -> {
            try {
                results.add(CompletableFuture.completedFuture(cartService.addToCart(user2.getEmail(), cartRequestDto1)));
            } finally {
                latch.countDown();
            }
        };

        executor.submit(task1);
        executor.submit(task2);

        latch.await();

        executor.shutdown();

        long successCount = results.stream().filter(f -> {
            try {
                return f.get().getStatusCode() == HttpStatus.OK;
            } catch (Exception e) {
                return false;
            }
        }).count();

        assertEquals(1, successCount, "Only one user should successfully add the book to the cart.");

        long failureCount = results.stream().filter(f -> {
            try {
                return f.get().getStatusCode() == HttpStatus.NO_CONTENT;
            } catch (Exception e) {
                return false;
            }
        }).count();

        assertEquals(1, failureCount, "One user should fail due to out-of-stock.");
    }


    @Test
    void addToCart_ExampleForCallingMultipleTimes()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);
        CartRequestDto cartRequestDto=CartRequestDto.builder().bookId(1L).build();
        HttpEntity<Object> httpEntity=new HttpEntity<>(cartRequestDto,httpHeaders);

        ResponseEntity<ResponseStructure<CartResponseDto>> response=restTemplate.exchange(baseUrl + "/addToCart", HttpMethod.POST, httpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponseDto>>() {});

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());
    }

    @Test
    void removeFromCart_ValidTest()
    {
        addToCart_ExampleForCallingMultipleTimes();
        addToCart_ExampleForCallingMultipleTimes();

        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);
        HttpEntity<Object> httpEntity=new HttpEntity<>(httpHeaders);

        ResponseEntity<ResponseStructure<CartResponseDto>> response=restTemplate.exchange(baseUrl + "/removeFromCart/1", HttpMethod.DELETE, httpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponseDto>>() {});

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());
        assertEquals("Book TEST has removed from the cart",response.getBody().getMessage());

        Book book=bookH2Repository.findById(1L).get();
        assertEquals(2,book.getBookQuantity());

        User user=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(1,user.getCarts().getFirst().getCartQuantity());


        ResponseEntity<ResponseStructure<CartResponseDto>> response2=restTemplate.exchange(baseUrl + "/removeFromCart/1", HttpMethod.DELETE, httpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponseDto>>() {});

        assertEquals(HttpStatus.OK,response2.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response2.getBody().getStatus());
        assertEquals("Book TEST has removed from the cart",response2.getBody().getMessage());

        Book book2=bookH2Repository.findById(1L).get();
        assertEquals(3,book2.getBookQuantity());

        User user2=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(0,user2.getCarts().size());


        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/removeFromCart/1", HttpMethod.DELETE, httpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponseDto>>() {}));
        assertEquals(HttpStatus.NOT_FOUND,exception.getStatusCode());
    }


    @Test
    void removeFromCart_IfCartNotFound()
    {
        addToCart_ExampleForCallingMultipleTimes();

        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);
        HttpEntity<Object> httpEntity=new HttpEntity<>(httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/removeFromCart/2", HttpMethod.DELETE, httpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponseDto>>() {}));
        assertEquals(HttpStatus.NOT_FOUND,exception.getStatusCode());
        exception.printStackTrace();
    }



    @Test
    void getCart_ValidTest()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);
        addToCart_ExampleForCallingMultipleTimes();
        addToCart_ExampleForCallingMultipleTimes();
        addToCart_ExampleForCallingMultipleTimes();

        CartRequestDto cartRequestDto=CartRequestDto.builder().bookId(2L).build();
        HttpEntity<Object> postHttpEntity=new HttpEntity<>(cartRequestDto,httpHeaders);

        ResponseEntity<ResponseStructure<CartResponseDto>> response1=restTemplate.exchange(baseUrl + "/addToCart", HttpMethod.POST, postHttpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponseDto>>() {});
        assertEquals(HttpStatus.OK,response1.getStatusCode());


        HttpEntity<Object> httpEntity=new HttpEntity<>(httpHeaders);

        ResponseEntity<ResponseStructure<List<CartResponseDto>>> response=restTemplate.exchange(baseUrl + "/getCart", HttpMethod.GET, httpEntity, new ParameterizedTypeReference<ResponseStructure<List<CartResponseDto>>>() {});

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(3,response.getBody().getData().getFirst().getCartQuantity());
        assertEquals(1,response.getBody().getData().getFirst().getCartId());
        assertEquals(1,response.getBody().getData().getFirst().getBookId());

        User user=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(2,user.getCarts().size());
    }


    @Test
    void getCart_CartIsEmpty()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);
        HttpEntity<Object> httpEntity=new HttpEntity<>(httpHeaders);

        ResponseEntity<ResponseStructure<List<CartResponseDto>>> response=restTemplate.exchange(baseUrl + "/getCart", HttpMethod.GET, httpEntity, new ParameterizedTypeReference<ResponseStructure<List<CartResponseDto>>>() {});

        assertEquals(HttpStatus.NO_CONTENT,response.getStatusCode());

        User user=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(0,user.getCarts().size());
    }

    @Test
    void getCart_TokenIsInvalid()
    {
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer token");
        HttpEntity<Object> httpEntity=new HttpEntity<>(httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/getCart", HttpMethod.GET, httpEntity, new ParameterizedTypeReference<ResponseStructure<List<CartResponseDto>>>() {}));
        assertEquals(HttpStatus.UNAUTHORIZED,exception.getStatusCode());
        exception.printStackTrace();
    }



    @Test
    void clearCart_ValidTest()
    {
        addToCart_ExampleForCallingMultipleTimes();
        addToCart_ExampleForCallingMultipleTimes();

        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);
        HttpEntity<Object> httpEntity=new HttpEntity<>(httpHeaders);

        CartRequestDto cartRequestDto=CartRequestDto.builder().bookId(2L).build();
        HttpEntity<Object> postHttpEntity=new HttpEntity<>(cartRequestDto,httpHeaders);

        ResponseEntity<ResponseStructure<CartResponseDto>> response=restTemplate.exchange(baseUrl + "/addToCart", HttpMethod.POST, postHttpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponseDto>>() {});
        assertEquals(HttpStatus.OK,response.getStatusCode());

        Book book=bookH2Repository.findById(1L).get();
        assertEquals(1,book.getBookQuantity());

        Book second=bookH2Repository.findById(2L).get();
        assertEquals(168,second.getBookQuantity());

        User user=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(2,user.getCarts().size());

        ResponseEntity<ResponseStructure<CartResponseDto>> response2=restTemplate.exchange(baseUrl + "/clearCart", HttpMethod.DELETE, httpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponseDto>>() {});

        assertEquals(HttpStatus.OK,response2.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response2.getBody().getStatus());
        assertEquals("Cart cleared successfully",response2.getBody().getMessage());

        Book book2=bookH2Repository.findById(1L).get();
        assertEquals(3,book2.getBookQuantity());

        Book secondBook=bookH2Repository.findById(2L).get();
        assertEquals(169,secondBook.getBookQuantity());

        User user2=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(0,user2.getCarts().size());
    }


    @Test
    void clearCart_IfCartIsEmpty()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);
        HttpEntity<Object> httpEntity=new HttpEntity<>(httpHeaders);

        User user=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(0,user.getCarts().size());

        ResponseEntity<ResponseStructure<CartResponseDto>> response2=restTemplate.exchange(baseUrl + "/clearCart", HttpMethod.DELETE, httpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponseDto>>() {});

        assertEquals(HttpStatus.NO_CONTENT,response2.getStatusCode());
    }

    @Test
    void clearCart_IfTokenIsInvalid()
    {
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer token");
        HttpEntity<Object> httpEntity=new HttpEntity<>(httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/clearCart", HttpMethod.DELETE, httpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponseDto>>() {}));
        assertEquals(HttpStatus.UNAUTHORIZED,exception.getStatusCode());
    }
}
