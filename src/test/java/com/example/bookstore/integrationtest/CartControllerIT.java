package com.example.bookstore.integrationtest;

import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.User;
import com.example.bookstore.integrationtest.h2repo.BookH2Repository;
import com.example.bookstore.integrationtest.h2repo.CartH2Repository;
import com.example.bookstore.integrationtest.h2repo.UserH2Repository;
import com.example.bookstore.requestdto.CartRequest;
import com.example.bookstore.requestdto.UserLoginEntity;
import com.example.bookstore.requestdto.UserRegisterEntity;
import com.example.bookstore.responsedto.CartResponse;
import com.example.bookstore.responsedto.LoginResponse;
import com.example.bookstore.responsedto.RegisterResponse;
import com.example.bookstore.service.CartService;
import com.example.bookstore.util.ResponseStructure;
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
class CartControllerIT
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
                .status(true)
                .bookLogo("URL").build();

        Book book2=Book.builder()
                .bookId(2L)
                .bookName("Habits")
                .bookPrice(249.49)
                .status(true)
                .bookAuthor("Zak crawly")
                .bookDescription("Cricket")
                .bookQuantity(169)
                .bookLogo("URL").build();

        Book book3=Book.builder()
                .bookId(3L)
                .bookName("Gotye")
                .bookPrice(789.49)
                .status(true)
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
            UserRegisterEntity userRegisterEntity = UserRegisterEntity.builder()
                    .firstName("Soul")
                    .lastName("Dinesh")
                    .dob(LocalDate.of(1992,8,24))
                    .email("dinesh@gmail.com")
                    .role("USER")
                    .password("Dinesh@090").build();

            ResponseEntity<ResponseStructure<RegisterResponse>> registerResponse = restTemplate.exchange( "http://localhost:"+port+"/register", HttpMethod.POST, new HttpEntity<>(userRegisterEntity), new ParameterizedTypeReference<ResponseStructure<RegisterResponse>>(){});

            assertEquals(HttpStatus.CREATED,registerResponse.getStatusCode());
            assertEquals(HttpStatus.CREATED.value(),registerResponse.getBody().getStatus());

            UserLoginEntity userLoginEntity = UserLoginEntity.builder()
                    .email("dinesh@gmail.com")
                    .password("Dinesh@090").build();

            ResponseEntity<ResponseStructure<LoginResponse>> loginResponse = restTemplate.exchange(  "http://localhost:"+port+"/login", HttpMethod.POST, new HttpEntity<>(userLoginEntity), new ParameterizedTypeReference<ResponseStructure<LoginResponse>>(){});

            assertEquals(HttpStatus.OK,loginResponse.getStatusCode());
            assertEquals(HttpStatus.OK.value(),loginResponse.getBody().getStatus());
            assertEquals("dinesh@gmail.com",loginResponse.getBody().getData().getEmail());
            assertEquals("USER",loginResponse.getBody().getData().getRole());
            authToken = loginResponse.getBody().getMessage();
        }
        return authToken;
    }


    @Test
    void addToCartValidTestMultipleTimesUntilBookOutOfStock()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);
        CartRequest cartRequest = CartRequest.builder().bookId(3L).build();
        HttpEntity<Object> httpEntity=new HttpEntity<>(cartRequest,httpHeaders);

        ResponseEntity<ResponseStructure<CartResponse>> response=restTemplate.exchange(baseUrl + "/addToCart", HttpMethod.POST, httpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());
        assertEquals(3,response.getBody().getData().getBookId(),"checking if the same book is added to cart or not");
        assertEquals(1,response.getBody().getData().getCartQuantity(),"checking the cart quantity");

        Book book=bookH2Repository.findById(3L).get();
        assertEquals(1,book.getBookQuantity(),"checking the book quantity as it should get reduced if user added to cart");

        User user=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(1,user.getCarts().size());


        CartRequest cartRequest2 = CartRequest.builder().bookId(3L).build();
        HttpEntity<Object> httpEntity2=new HttpEntity<>(cartRequest2,httpHeaders);

        ResponseEntity<ResponseStructure<CartResponse>> response2=restTemplate.exchange(baseUrl + "/addToCart", HttpMethod.POST, httpEntity2, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});

        assertEquals(HttpStatus.OK,response2.getStatusCode());
        Book book2=bookH2Repository.findById(3L).get();
        assertEquals(0,book2.getBookQuantity());

        User user2=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(1,user2.getCarts().size());


        CartRequest cartRequest3 = CartRequest.builder().bookId(3L).build();
        HttpEntity<Object> httpEntity3=new HttpEntity<>(cartRequest3,httpHeaders);


        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/addToCart", HttpMethod.POST, httpEntity3, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {}));
        assertEquals(HttpStatus.CONFLICT,exception.getStatusCode(),"user wont be able to add the book to cart if its out of quantity");

        Book book3=bookH2Repository.findById(3L).get();
        assertEquals(0,book3.getBookQuantity(),"If user cant add the book to cart ,it should be 0");
    }


    @Test
    void testAddToCartConcurrencyWhenTwoUsersTryToBuyTheLastBook() throws InterruptedException
    {
        Long bookId = 123L;
        CartRequest cartRequest1 = CartRequest.builder()
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

        List<Future<ResponseEntity<ResponseStructure<CartResponse>>>> results = new ArrayList<>();

        Runnable task1 = () -> {
            try {
                results.add(CompletableFuture.completedFuture(cartService.addToCart(user1.getEmail(), cartRequest1)));
            } finally {
                latch.countDown();
            }
        };
        Runnable task2 = () -> {
            try {
                results.add(CompletableFuture.completedFuture(cartService.addToCart(user2.getEmail(), cartRequest1)));
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
                return f.get().getStatusCode() == HttpStatus.CONFLICT;
            } catch (Exception e) {
                return false;
            }
        }).count();

        assertEquals(1, failureCount, "One user should fail due to out-of-stock.");
    }


    @Test
    void addToCartExampleForCallingMultipleTimes()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);
        CartRequest cartRequest = CartRequest.builder().bookId(1L).build();
        HttpEntity<Object> httpEntity=new HttpEntity<>(cartRequest,httpHeaders);

        ResponseEntity<ResponseStructure<CartResponse>> response=restTemplate.exchange(baseUrl + "/addToCart", HttpMethod.POST, httpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());
    }

    @Test
    void removeFromCartValidTest()
    {
        addToCartExampleForCallingMultipleTimes();
        addToCartExampleForCallingMultipleTimes();

        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);
        HttpEntity<Object> httpEntity=new HttpEntity<>(httpHeaders);

        ResponseEntity<ResponseStructure<CartResponse>> response=restTemplate.exchange(baseUrl + "/removeFromCart/1", HttpMethod.DELETE, httpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());
        assertEquals("Book TEST has removed from the cart",response.getBody().getMessage());

        Book book=bookH2Repository.findById(1L).get();
        assertEquals(2,book.getBookQuantity());

        User user=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(1,user.getCarts().getFirst().getCartQuantity());


        ResponseEntity<ResponseStructure<CartResponse>> response2=restTemplate.exchange(baseUrl + "/removeFromCart/1", HttpMethod.DELETE, httpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});

        assertEquals(HttpStatus.OK,response2.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response2.getBody().getStatus());
        assertEquals("Book TEST has removed from the cart",response2.getBody().getMessage());

        Book book2=bookH2Repository.findById(1L).get();
        assertEquals(3,book2.getBookQuantity());

        User user2=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(0,user2.getCarts().size());


        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/removeFromCart/1", HttpMethod.DELETE, httpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {}));
        assertEquals(HttpStatus.NOT_FOUND,exception.getStatusCode());
    }


    @Test
    void removeFromCartIfCartNotFound()
    {
        addToCartExampleForCallingMultipleTimes();

        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);
        HttpEntity<Object> httpEntity=new HttpEntity<>(httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/removeFromCart/2", HttpMethod.DELETE, httpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {}));
        assertEquals(HttpStatus.NOT_FOUND,exception.getStatusCode());
        exception.printStackTrace();
    }



    @Test
    void getCartValidTest()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);
        addToCartExampleForCallingMultipleTimes();
        addToCartExampleForCallingMultipleTimes();
        addToCartExampleForCallingMultipleTimes();

        CartRequest cartRequest = CartRequest.builder().bookId(2L).build();
        HttpEntity<Object> postHttpEntity=new HttpEntity<>(cartRequest,httpHeaders);

        ResponseEntity<ResponseStructure<CartResponse>> response1=restTemplate.exchange(baseUrl + "/addToCart", HttpMethod.POST, postHttpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});
        assertEquals(HttpStatus.OK,response1.getStatusCode());


        HttpEntity<Object> httpEntity=new HttpEntity<>(httpHeaders);

        ResponseEntity<ResponseStructure<List<CartResponse>>> response=restTemplate.exchange(baseUrl + "/getCart", HttpMethod.GET, httpEntity, new ParameterizedTypeReference<ResponseStructure<List<CartResponse>>>() {});

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(3,response.getBody().getData().getFirst().getCartQuantity());
        assertEquals(1,response.getBody().getData().getFirst().getCartId());
        assertEquals(1,response.getBody().getData().getFirst().getBookId());

        User user=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(2,user.getCarts().size());
    }


    @Test
    void getCartCartIsEmpty()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);
        HttpEntity<Object> httpEntity=new HttpEntity<>(httpHeaders);

        ResponseEntity<ResponseStructure<List<CartResponse>>> response=restTemplate.exchange(baseUrl + "/getCart", HttpMethod.GET, httpEntity, new ParameterizedTypeReference<ResponseStructure<List<CartResponse>>>() {});

        assertEquals(HttpStatus.NO_CONTENT,response.getStatusCode());

        User user=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(0,user.getCarts().size());
    }

    @Test
    void getCartTokenIsInvalid()
    {
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer token");
        HttpEntity<Object> httpEntity=new HttpEntity<>(httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/getCart", HttpMethod.GET, httpEntity, new ParameterizedTypeReference<ResponseStructure<List<CartResponse>>>() {}));
        assertEquals(HttpStatus.UNAUTHORIZED,exception.getStatusCode());
        exception.printStackTrace();
    }



    @Test
    void clearCartValidTest()
    {
        addToCartExampleForCallingMultipleTimes();
        addToCartExampleForCallingMultipleTimes();

        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);
        HttpEntity<Object> httpEntity=new HttpEntity<>(httpHeaders);

        CartRequest cartRequest = CartRequest.builder().bookId(2L).build();
        HttpEntity<Object> postHttpEntity=new HttpEntity<>(cartRequest,httpHeaders);

        ResponseEntity<ResponseStructure<CartResponse>> response=restTemplate.exchange(baseUrl + "/addToCart", HttpMethod.POST, postHttpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});
        assertEquals(HttpStatus.OK,response.getStatusCode());

        Book book=bookH2Repository.findById(1L).get();
        assertEquals(1,book.getBookQuantity());

        Book second=bookH2Repository.findById(2L).get();
        assertEquals(168,second.getBookQuantity());

        User user=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(2,user.getCarts().size());

        ResponseEntity<ResponseStructure<CartResponse>> response2=restTemplate.exchange(baseUrl + "/clearCart", HttpMethod.DELETE, httpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});

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
    void clearCartIfCartIsEmpty()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);
        HttpEntity<Object> httpEntity=new HttpEntity<>(httpHeaders);

        User user=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(0,user.getCarts().size());

        ResponseEntity<ResponseStructure<CartResponse>> response2=restTemplate.exchange(baseUrl + "/clearCart", HttpMethod.DELETE, httpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});

        assertEquals(HttpStatus.NO_CONTENT,response2.getStatusCode());
    }

    @Test
    void clearCartIfTokenIsInvalid()
    {
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer token");
        HttpEntity<Object> httpEntity=new HttpEntity<>(httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/clearCart", HttpMethod.DELETE, httpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {}));
        assertEquals(HttpStatus.UNAUTHORIZED,exception.getStatusCode());
    }
}
