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
import org.junit.jupiter.api.AfterEach;
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
class CartControllerITTests
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

    @BeforeAll
    static void init()
    {
        restTemplate=new RestTemplate();
    }

    @BeforeEach
    void setUp()
    {
        baseUrl=baseUrl.concat(":").concat(port+"").concat("/cart");
        userH2Repository.deleteAll();
        cartH2Repository.deleteAll();
        bookH2Repository.deleteAll();
    }

    @AfterEach
    void tearDown()
    {
        userH2Repository.deleteAll();
        cartH2Repository.deleteAll();
        bookH2Repository.deleteAll();
    }

    protected String getAuthToken()
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
        return loginResponse.getBody().getMessage();
    }


    @Test
    void addToCartValidTestMultipleTimesUntilBookOutOfStock()
    {
        Book book12=Book.builder()
                .bookName("Gotye")
                .bookPrice(789.49)
                .status(true)
                .bookAuthor("Ryan")
                .bookDescription("Deadpool")
                .bookQuantity(2)
                .bookLogo("URL").build();

        Book saved3=bookH2Repository.save(book12);

        String authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        CartRequest cartRequest = CartRequest.builder().bookId(saved3.getBookId()).build();
        HttpEntity<Object> httpEntity=new HttpEntity<>(cartRequest,httpHeaders);
        ResponseEntity<ResponseStructure<CartResponse>> response=restTemplate.exchange(baseUrl + "/addToCart", HttpMethod.POST, httpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());
        assertEquals(saved3.getBookId(),response.getBody().getData().getBookId(),"checking if the same book is added to cart or not");
        assertEquals(1,response.getBody().getData().getCartQuantity(),"checking the cart quantity");

        User user=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(1,user.getCarts().size());


        CartRequest cartRequest2 = CartRequest.builder().bookId(saved3.getBookId()).build();
        HttpEntity<Object> httpEntity2=new HttpEntity<>(cartRequest2,httpHeaders);

        ResponseEntity<ResponseStructure<CartResponse>> response2=restTemplate.exchange(baseUrl + "/addToCart", HttpMethod.POST, httpEntity2, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});

        assertEquals(HttpStatus.OK,response2.getStatusCode());
        Book book2=bookH2Repository.findById(saved3.getBookId()).get();
        assertEquals(0,book2.getBookQuantity());

        User user2=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(1,user2.getCarts().size());


        CartRequest cartRequest3 = CartRequest.builder().bookId(saved3.getBookId()).build();
        HttpEntity<Object> httpEntity3=new HttpEntity<>(cartRequest3,httpHeaders);


        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/addToCart", HttpMethod.POST, httpEntity3, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {}));
        assertEquals(HttpStatus.CONFLICT,exception.getStatusCode(),"user wont be able to add the book to cart if its out of quantity");

        Book book3=bookH2Repository.findById(saved3.getBookId()).get();
        assertEquals(0,book3.getBookQuantity(),"If user cant add the book to cart ,it should be 0");
    }


    @Test
    void testAddToCartConcurrencyWhenTwoUsersTryToBuyTheLastBook() throws InterruptedException
    {
        User user1 = new User();
        user1.setEmail("user1@gmail.com");
        user1.setCarts(new ArrayList<>());
        userH2Repository.save(user1);

        User user2 = new User();
        user2.setEmail("user2@gmail.com");
        user2.setCarts(new ArrayList<>());
        userH2Repository.save(user2);

        Book book = new Book();
        book.setBookQuantity(1);
        Book saved=bookH2Repository.save(book);

        CartRequest cartRequest1 = CartRequest.builder()
                .bookId(saved.getBookId())
                .build();

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
        Book book10=Book.builder()
                .bookName("TEST")
                .bookPrice(199.3)
                .bookAuthor("Chandu")
                .bookDescription("Atom")
                .bookQuantity(5)
                .status(true)
                .bookLogo("URL").build();
        Book saved=bookH2Repository.save(book10);

        String authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);
        CartRequest cartRequest = CartRequest.builder().bookId(saved.getBookId()).build();
        HttpEntity<Object> httpEntity=new HttpEntity<>(cartRequest,httpHeaders);

        ResponseEntity<ResponseStructure<CartResponse>> response=restTemplate.exchange(baseUrl + "/addToCart", HttpMethod.POST, httpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());
    }

    @Test
    void removeFromCartValidTest()
    {
        Book book10=Book.builder()
                .bookName("TEST")
                .bookPrice(199.3)
                .bookAuthor("Chandu")
                .bookDescription("Atom")
                .bookQuantity(5)
                .status(true)
                .bookLogo("URL").build();

        Book saved1=bookH2Repository.save(book10);

        String authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);


        //Adding to cart 1st time
        CartRequest cartRequest = CartRequest.builder().bookId(saved1.getBookId()).build();
        HttpEntity<Object> httpEntity=new HttpEntity<>(cartRequest,httpHeaders);
        ResponseEntity<ResponseStructure<CartResponse>> response=restTemplate.exchange(baseUrl + "/addToCart", HttpMethod.POST, httpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());
        assertEquals(saved1.getBookId(),response.getBody().getData().getBookId(),"checking if the same book is added to cart or not");
        assertEquals(1,response.getBody().getData().getCartQuantity(),"checking the cart quantity");
        Long cartId=response.getBody().getData().getCartId();


        //Adding to cart 2nd time
        CartRequest cartRequest2 = CartRequest.builder().bookId(saved1.getBookId()).build();
        HttpEntity<Object> httpEntity2=new HttpEntity<>(cartRequest2,httpHeaders);
        ResponseEntity<ResponseStructure<CartResponse>> response2=restTemplate.exchange(baseUrl + "/addToCart", HttpMethod.POST, httpEntity2, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});
        assertEquals(HttpStatus.OK,response2.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response2.getBody().getStatus());
        assertEquals(saved1.getBookId(),response2.getBody().getData().getBookId(),"checking if the same book is added to cart or not");
        assertEquals(2,response2.getBody().getData().getCartQuantity(),"checking the cart quantity");
        Long cartId2=response2.getBody().getData().getCartId();


        //This actual test
        HttpEntity<Object> httpEntity3=new HttpEntity<>(httpHeaders);
        ResponseEntity<ResponseStructure<CartResponse>> response3=restTemplate.exchange(baseUrl + "/removeFromCart/"+cartId, HttpMethod.DELETE, httpEntity3, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});
        assertEquals(HttpStatus.OK,response3.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response3.getBody().getStatus());
        assertEquals("Book "+saved1.getBookName()+" has removed from the cart",response3.getBody().getMessage());

        Book book=bookH2Repository.findById(saved1.getBookId()).get();
        assertEquals(4,book.getBookQuantity());

        User user=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(1,user.getCarts().getFirst().getCartQuantity());


        ResponseEntity<ResponseStructure<CartResponse>> response4=restTemplate.exchange(baseUrl + "/removeFromCart/"+cartId2, HttpMethod.DELETE, httpEntity3, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});
        assertEquals(HttpStatus.OK,response4.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response4.getBody().getStatus());
        assertEquals("Book "+saved1.getBookName()+" has removed from the cart",response4.getBody().getMessage());

        Book book2=bookH2Repository.findById(saved1.getBookId()).get();
        assertEquals(5,book2.getBookQuantity());

        User user2=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(0,user2.getCarts().size());


        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/removeFromCart/1", HttpMethod.DELETE, httpEntity3, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {}));
        assertEquals(HttpStatus.NOT_FOUND,exception.getStatusCode());
    }


    @Test
    void removeFromCartIfCartNotFound()
    {
        Book book10=Book.builder()
                .bookName("TEST")
                .bookPrice(199.3)
                .bookAuthor("Chandu")
                .bookDescription("Atom")
                .bookQuantity(5)
                .status(true)
                .bookLogo("URL").build();

        Book saved=bookH2Repository.save(book10);

        String authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        CartRequest cartRequest = CartRequest.builder().bookId(saved.getBookId()).build();
        HttpEntity<Object> httpEntity=new HttpEntity<>(cartRequest,httpHeaders);
        ResponseEntity<ResponseStructure<CartResponse>> response=restTemplate.exchange(baseUrl + "/addToCart", HttpMethod.POST, httpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());

        //This actual test
        HttpEntity<Object> httpEntity2=new HttpEntity<>(httpHeaders);
        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/removeFromCart/2", HttpMethod.DELETE, httpEntity2, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {}));
        assertEquals(HttpStatus.NOT_FOUND,exception.getStatusCode());
        exception.printStackTrace();
    }



    @Test
    void getCartValidTest()
    {
        Book book10=Book.builder()
                .bookName("TEST")
                .bookPrice(199.3)
                .bookAuthor("Chandu")
                .bookDescription("Atom")
                .bookQuantity(5)
                .status(true)
                .bookLogo("URL").build();

        Book book11=Book.builder()
                .bookName("Habits")
                .bookPrice(249.49)
                .status(true)
                .bookAuthor("Zak crawly")
                .bookDescription("Cricket")
                .bookQuantity(169)
                .bookLogo("URL").build();

        Book saved1=bookH2Repository.save(book10);
        Book saved2=bookH2Repository.save(book11);


        String authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        CartRequest cartRequest1 = CartRequest.builder().bookId(saved1.getBookId()).build();
        HttpEntity<Object> httpEntity=new HttpEntity<>(cartRequest1,httpHeaders);
        ResponseEntity<ResponseStructure<CartResponse>> response=restTemplate.exchange(baseUrl + "/addToCart", HttpMethod.POST, httpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());

        CartRequest cartRequest2 = CartRequest.builder().bookId(saved1.getBookId()).build();
        HttpEntity<Object> httpEntity2=new HttpEntity<>(cartRequest2,httpHeaders);
        ResponseEntity<ResponseStructure<CartResponse>> response2=restTemplate.exchange(baseUrl + "/addToCart", HttpMethod.POST, httpEntity2, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});
        assertEquals(HttpStatus.OK,response2.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response2.getBody().getStatus());

        CartRequest cartRequest3 = CartRequest.builder().bookId(saved1.getBookId()).build();
        HttpEntity<Object> httpEntity3=new HttpEntity<>(cartRequest3,httpHeaders);
        ResponseEntity<ResponseStructure<CartResponse>> response3=restTemplate.exchange(baseUrl + "/addToCart", HttpMethod.POST, httpEntity3, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});
        assertEquals(HttpStatus.OK,response3.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response3.getBody().getStatus());



        CartRequest cartRequest = CartRequest.builder().bookId(saved2.getBookId()).build();
        HttpEntity<Object> postHttpEntity=new HttpEntity<>(cartRequest,httpHeaders);
        ResponseEntity<ResponseStructure<CartResponse>> response1=restTemplate.exchange(baseUrl + "/addToCart", HttpMethod.POST, postHttpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});
        assertEquals(HttpStatus.OK,response1.getStatusCode());


        HttpEntity<Object> httpEntity4=new HttpEntity<>(httpHeaders);
        ResponseEntity<ResponseStructure<List<CartResponse>>> response4=restTemplate.exchange(baseUrl + "/getCart", HttpMethod.GET, httpEntity4, new ParameterizedTypeReference<ResponseStructure<List<CartResponse>>>() {});
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(3,response4.getBody().getData().getFirst().getCartQuantity());
        assertEquals(saved1.getBookId(),response4.getBody().getData().getFirst().getBookId());

        User user=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(2,user.getCarts().size());
    }


    @Test
    void getCartCartIsEmpty()
    {
        String authToken=getAuthToken();
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
        Book book10=Book.builder()
                .bookName("TEST")
                .bookPrice(199.3)
                .bookAuthor("Chandu")
                .bookDescription("Atom")
                .bookQuantity(3)
                .status(true)
                .bookLogo("URL").build();

        Book book11=Book.builder()
                .bookName("Habits")
                .bookPrice(249.49)
                .status(true)
                .bookAuthor("Zak crawly")
                .bookDescription("Cricket")
                .bookQuantity(169)
                .bookLogo("URL").build();

        Book saved1=bookH2Repository.save(book10);
        Book saved2=bookH2Repository.save(book11);

        String authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);
        HttpEntity<Object> httpEntity=new HttpEntity<>(httpHeaders);

        CartRequest cartRequest1 = CartRequest.builder().bookId(saved1.getBookId()).build();
        HttpEntity<Object> httpEntity1=new HttpEntity<>(cartRequest1,httpHeaders);
        ResponseEntity<ResponseStructure<CartResponse>> response1=restTemplate.exchange(baseUrl + "/addToCart", HttpMethod.POST, httpEntity1, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});
        assertEquals(HttpStatus.OK,response1.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response1.getBody().getStatus());


        CartRequest cartRequest2 = CartRequest.builder().bookId(saved1.getBookId()).build();
        HttpEntity<Object> httpEntity2=new HttpEntity<>(cartRequest2,httpHeaders);
        ResponseEntity<ResponseStructure<CartResponse>> response2=restTemplate.exchange(baseUrl + "/addToCart", HttpMethod.POST, httpEntity2, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});
        assertEquals(HttpStatus.OK,response2.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response2.getBody().getStatus());


        CartRequest cartRequest = CartRequest.builder().bookId(saved2.getBookId()).build();
        HttpEntity<Object> postHttpEntity=new HttpEntity<>(cartRequest,httpHeaders);

        ResponseEntity<ResponseStructure<CartResponse>> response=restTemplate.exchange(baseUrl + "/addToCart", HttpMethod.POST, postHttpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});
        assertEquals(HttpStatus.OK,response.getStatusCode());

        Book book=bookH2Repository.findById(saved1.getBookId()).get();
        assertEquals(1,book.getBookQuantity());

        Book second=bookH2Repository.findById(saved2.getBookId()).get();
        assertEquals(168,second.getBookQuantity());

        User user=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(2,user.getCarts().size());

        ResponseEntity<ResponseStructure<CartResponse>> response4=restTemplate.exchange(baseUrl + "/clearCart", HttpMethod.DELETE, httpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});

        assertEquals(HttpStatus.OK,response4.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response4.getBody().getStatus());
        assertEquals("Cart cleared successfully",response4.getBody().getMessage());

        Book book2=bookH2Repository.findById(saved1.getBookId()).get();
        assertEquals(3,book2.getBookQuantity());

        Book secondBook=bookH2Repository.findById(saved2.getBookId()).get();
        assertEquals(169,secondBook.getBookQuantity());

        User user2=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(0,user2.getCarts().size());
    }


    @Test
    void clearCartIfCartIsEmpty()
    {
        String authToken=getAuthToken();
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
