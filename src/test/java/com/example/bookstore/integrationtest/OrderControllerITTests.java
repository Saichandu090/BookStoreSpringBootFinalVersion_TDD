package com.example.bookstore.integrationtest;

import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.User;
import com.example.bookstore.integrationtest.h2repo.BookH2Repository;
import com.example.bookstore.integrationtest.h2repo.CartH2Repository;
import com.example.bookstore.integrationtest.h2repo.OrderH2Repository;
import com.example.bookstore.integrationtest.h2repo.UserH2Repository;
import com.example.bookstore.requestdto.*;
import com.example.bookstore.responsedto.*;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderControllerITTests
{
    @LocalServerPort
    private int port;

    @Autowired
    private UserH2Repository userH2Repository;

    @Autowired
    private BookH2Repository bookH2Repository;

    @Autowired
    private OrderH2Repository orderH2Repository;

    @Autowired
    private CartH2Repository cartH2Repository;

    private String baseUrl="http://localhost";

    private static RestTemplate restTemplate;

    @BeforeAll
    public static void init()
    {
        restTemplate=new RestTemplate();
    }

    @BeforeEach
    void setUp()
    {
        baseUrl=baseUrl.concat(":").concat(port+"").concat("/order");
        userH2Repository.deleteAll();
        cartH2Repository.deleteAll();
        orderH2Repository.deleteAll();
    }

    @AfterEach
    void tearDown()
    {
        userH2Repository.deleteAll();
        cartH2Repository.deleteAll();
        orderH2Repository.deleteAll();
    }


    @BeforeEach
    void addBooksToRepository()
    {
        Book book1=Book.builder()
                .bookName("TEST")
                .bookPrice(199.3)
                .bookAuthor("Chandu")
                .bookDescription("Atom")
                .bookQuantity(10)
                .status(true)
                .bookLogo("URL").build();

        Book book2=Book.builder()
                .bookName("Habits")
                .bookPrice(249.49)
                .status(true)
                .bookAuthor("Zak crawly")
                .bookDescription("Cricket")
                .bookQuantity(169)
                .bookLogo("URL").build();

        Book book3=Book.builder()
                .bookName("Gotye")
                .bookPrice(789.49)
                .bookAuthor("Ryan")
                .status(true)
                .bookDescription("Deadpool")
                .bookQuantity(2)
                .bookLogo("URL").build();

        bookH2Repository.save(book1);
        bookH2Repository.save(book2);
        bookH2Repository.save(book3);
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
    void placeOrderValidTestAlsoTestedIfAddressIsInvalidIfCartIsEmpty()
    {
        //adding books to cart to place the order
        String authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        CartRequest cartRequest = CartRequest.builder().bookId(2L).build();
        HttpEntity<Object> httpEntity=new HttpEntity<>(cartRequest,httpHeaders);
        ResponseEntity<ResponseStructure<CartResponse>> response=restTemplate.exchange(  "http://localhost:"+port+"/cart/addToCart", HttpMethod.POST, httpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());
        assertEquals(2,response.getBody().getData().getBookId());
        assertEquals(1,response.getBody().getData().getCartQuantity());

        CartRequest cartRequest2 = CartRequest.builder().bookId(1L).build();
        HttpEntity<Object> httpEntity2=new HttpEntity<>(cartRequest2,httpHeaders);
        ResponseEntity<ResponseStructure<CartResponse>> response2=restTemplate.exchange("http://localhost:"+port+"/cart/addToCart", HttpMethod.POST, httpEntity2, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});
        assertEquals(HttpStatus.OK,response2.getStatusCode());

        //Adding one address for user
        AddressRequest addressRequest = AddressRequest.builder()
                .streetName("Baner")
                .city("Pune")
                .pinCode(414004)
                .state("Maharastra").build();
        HttpEntity<Object> httpEntity3 = new HttpEntity<>(addressRequest,httpHeaders);
        ResponseEntity<ResponseStructure<AddressResponse>> response3 = restTemplate.exchange( "http://localhost:"+port+"/address/addAddress", HttpMethod.POST, httpEntity3,
                new ParameterizedTypeReference<ResponseStructure<AddressResponse>>() {});
        assertEquals(HttpStatus.CREATED,response3.getStatusCode());
        Long addressId=response3.getBody().getData().getAddressId();


        //Checking everything if user is holding or not
        User user=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(2,user.getCarts().size());
        assertEquals(1,user.getAddresses().size());


        //Placing an order with added address
        OrderRequest orderRequest = OrderRequest.builder().addressId(addressId).build();
        HttpEntity<Object> httpEntity4=new HttpEntity<>(orderRequest,httpHeaders);
        ResponseEntity<ResponseStructure<OrderResponse>> response4=restTemplate.exchange(baseUrl + "/placeOrder", HttpMethod.POST, httpEntity4, new ParameterizedTypeReference<ResponseStructure<OrderResponse>>() {});
        assertEquals(HttpStatus.CREATED,response4.getStatusCode());
        assertEquals("Baner",response4.getBody().getData().getOrderAddress().getStreetName());
        assertEquals(false,response4.getBody().getData().getCancelOrder());
        assertEquals(2,response4.getBody().getData().getOrderBooks().size(),"Checking the quantity of books ordered");

        User user1=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(0,user1.getCarts().size());
        assertEquals(1,user1.getAddresses().size());
        assertEquals(1,user1.getOrders().size());


        //Trying again if cart is empty
        OrderRequest orderRequest1 = OrderRequest.builder().addressId(addressId).build();
        HttpEntity<Object> httpEntity5=new HttpEntity<>(orderRequest1,httpHeaders);
        ResponseEntity<ResponseStructure<OrderResponse>> response5=restTemplate.exchange(baseUrl + "/placeOrder", HttpMethod.POST, httpEntity5, new ParameterizedTypeReference<ResponseStructure<OrderResponse>>() {});
        assertEquals(HttpStatus.NO_CONTENT,response5.getStatusCode(),"Once the order is placed cart should get cleared and cart is empty should be displayed");


        //Test if address id is invalid
        OrderRequest orderRequest2 = OrderRequest.builder().addressId(99L).build();
        HttpEntity<Object> httpEntity6=new HttpEntity<>(orderRequest2,httpHeaders);
        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/placeOrder", HttpMethod.POST, httpEntity6, new ParameterizedTypeReference<ResponseStructure<OrderResponse>>() {}));
        assertEquals(HttpStatus.NOT_FOUND,exception.getStatusCode(),"If the provided addressId is not found or not related to user");
        String responseBody = exception.getResponseBodyAsString();
        assertTrue(responseBody.contains("\"message\":\"Address not found with Id 99\""));
    }


    @Test
    void cancelOrderValidTest()
    {
        String authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        CartRequest cartRequest = CartRequest.builder().bookId(3L).build();
        HttpEntity<Object> httpEntity=new HttpEntity<>(cartRequest,httpHeaders);
        ResponseEntity<ResponseStructure<CartResponse>> response=restTemplate.exchange(  "http://localhost:"+port+"/cart/addToCart", HttpMethod.POST, httpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());
        assertEquals(3,response.getBody().getData().getBookId());
        assertEquals(1,response.getBody().getData().getCartQuantity());

        CartRequest cartRequest2 = CartRequest.builder().bookId(1L).build();
        HttpEntity<Object> httpEntity2=new HttpEntity<>(cartRequest2,httpHeaders);
        ResponseEntity<ResponseStructure<CartResponse>> response2=restTemplate.exchange("http://localhost:"+port+"/cart/addToCart", HttpMethod.POST, httpEntity2, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});
        assertEquals(HttpStatus.OK,response2.getStatusCode());

        //Adding one address for user
        AddressRequest addressRequest = AddressRequest.builder()
                .streetName("Baner")
                .city("Pune")
                .pinCode(414004)
                .state("Maharastra").build();
        HttpEntity<Object> httpEntity3 = new HttpEntity<>(addressRequest,httpHeaders);
        ResponseEntity<ResponseStructure<AddressResponse>> response3 = restTemplate.exchange( "http://localhost:"+port+"/address/addAddress", HttpMethod.POST, httpEntity3,
                new ParameterizedTypeReference<ResponseStructure<AddressResponse>>() {});
        assertEquals(HttpStatus.CREATED,response3.getStatusCode());
        Long addressId=response3.getBody().getData().getAddressId();


        //Checking everything if user is holding or not
        User user=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(2,user.getCarts().size());
        assertEquals(1,user.getAddresses().size());


        //Placing an order with added address
        OrderRequest orderRequest = OrderRequest.builder().addressId(addressId).build();
        HttpEntity<Object> httpEntity4=new HttpEntity<>(orderRequest,httpHeaders);
        ResponseEntity<ResponseStructure<OrderResponse>> response4=restTemplate.exchange(baseUrl + "/placeOrder", HttpMethod.POST, httpEntity4, new ParameterizedTypeReference<ResponseStructure<OrderResponse>>() {});
        assertEquals(HttpStatus.CREATED,response4.getStatusCode());
        assertEquals("Baner",response4.getBody().getData().getOrderAddress().getStreetName());
        assertEquals(false,response4.getBody().getData().getCancelOrder());
        assertEquals(2,response4.getBody().getData().getOrderBooks().size(),"Checking the quantity of books ordered");

        Long orderId=response4.getBody().getData().getOrderId();

        //Cancelling order

        HttpEntity<Object> entity=new HttpEntity<>(httpHeaders);
        ResponseEntity<ResponseStructure<OrderResponse>> response9=restTemplate.exchange(baseUrl + "/cancelOrder/"+orderId, HttpMethod.DELETE, entity, new ParameterizedTypeReference<ResponseStructure<OrderResponse>>() {});
        assertEquals(HttpStatus.OK,response9.getStatusCode());
        assertTrue(response9.getBody().getData().getCancelOrder());

        User user1=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(0,user1.getCarts().size(),"user cart should get cleared after placing an order successfully");
        assertEquals(1,user1.getAddresses().size());
        assertEquals(1,user1.getOrders().size(),"user should hold one order now");


        //Testing to cancel order which is already cancelled
        ResponseEntity<ResponseStructure<OrderResponse>> response7=restTemplate.exchange(baseUrl + "/cancelOrder/"+orderId, HttpMethod.DELETE, httpEntity, new ParameterizedTypeReference<ResponseStructure<OrderResponse>>() {});
        assertEquals(HttpStatus.NO_CONTENT,response7.getStatusCode(),"If request is successful but the order is already cancelled");
    }


    @Test
    void cancelOrderIfOrderIdIsInvalid()
    {
        String authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        CartRequest cartRequest = CartRequest.builder().bookId(1L).build();
        HttpEntity<Object> httpEntity=new HttpEntity<>(cartRequest,httpHeaders);
        ResponseEntity<ResponseStructure<CartResponse>> response=restTemplate.exchange(  "http://localhost:"+port+"/cart/addToCart", HttpMethod.POST, httpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());
        assertEquals(1,response.getBody().getData().getBookId());
        assertEquals(1,response.getBody().getData().getCartQuantity());

        CartRequest cartRequest2 = CartRequest.builder().bookId(1L).build();
        HttpEntity<Object> httpEntity2=new HttpEntity<>(cartRequest2,httpHeaders);
        ResponseEntity<ResponseStructure<CartResponse>> response2=restTemplate.exchange("http://localhost:"+port+"/cart/addToCart", HttpMethod.POST, httpEntity2, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});
        assertEquals(HttpStatus.OK,response2.getStatusCode());

        //Adding one address for user
        AddressRequest addressRequest = AddressRequest.builder()
                .streetName("Baner")
                .city("Pune")
                .pinCode(414004)
                .state("Maharastra").build();
        HttpEntity<Object> httpEntity3 = new HttpEntity<>(addressRequest,httpHeaders);
        ResponseEntity<ResponseStructure<AddressResponse>> response3 = restTemplate.exchange( "http://localhost:"+port+"/address/addAddress", HttpMethod.POST, httpEntity3,
                new ParameterizedTypeReference<ResponseStructure<AddressResponse>>() {});
        assertEquals(HttpStatus.CREATED,response3.getStatusCode());
        Long addressId=response3.getBody().getData().getAddressId();


        //Checking everything if user is holding or not
        User user=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(1,user.getCarts().size());
        assertEquals(1,user.getAddresses().size());


        //Placing an order with added address
        OrderRequest orderRequest = OrderRequest.builder().addressId(addressId).build();
        HttpEntity<Object> httpEntity4=new HttpEntity<>(orderRequest,httpHeaders);
        ResponseEntity<ResponseStructure<OrderResponse>> response4=restTemplate.exchange(baseUrl + "/placeOrder", HttpMethod.POST, httpEntity4, new ParameterizedTypeReference<ResponseStructure<OrderResponse>>() {});
        assertEquals(HttpStatus.CREATED,response4.getStatusCode());
        assertEquals("Baner",response4.getBody().getData().getOrderAddress().getStreetName());
        assertEquals(false,response4.getBody().getData().getCancelOrder());
        assertEquals(1,response4.getBody().getData().getOrderBooks().size(),"Checking the quantity of books ordered");


        //This test
        HttpEntity<Object> httpEntity5=new HttpEntity<>(httpHeaders);
        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/cancelOrder/10", HttpMethod.DELETE, httpEntity5, new ParameterizedTypeReference<ResponseStructure<String>>() {}));
        assertEquals(HttpStatus.NOT_FOUND,exception.getStatusCode(),"If order is not found by id or not in the list of user orders");
        String responseBody = exception.getResponseBodyAsString();
        assertTrue(responseBody.contains("\"message\":\"Order not found\""));
    }

    @Test
    void cancelOrderIfTokenIsInvalidInvalid()
    {
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer token");
        HttpEntity<Object> httpEntity=new HttpEntity<>(httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/cancelOrder/1", HttpMethod.DELETE, httpEntity, new ParameterizedTypeReference<ResponseStructure<String>>() {}));
        assertEquals(HttpStatus.UNAUTHORIZED,exception.getStatusCode(),"If token is missing or invalid");
    }


    @Test
    void getOrderValidTest()
    {
        String authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        CartRequest cartRequest = CartRequest.builder().bookId(3L).build();
        HttpEntity<Object> httpEntity=new HttpEntity<>(cartRequest,httpHeaders);
        ResponseEntity<ResponseStructure<CartResponse>> response=restTemplate.exchange(  "http://localhost:"+port+"/cart/addToCart", HttpMethod.POST, httpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());
        assertEquals(3,response.getBody().getData().getBookId());
        assertEquals(1,response.getBody().getData().getCartQuantity());

        CartRequest cartRequest2 = CartRequest.builder().bookId(1L).build();
        HttpEntity<Object> httpEntity2=new HttpEntity<>(cartRequest2,httpHeaders);
        ResponseEntity<ResponseStructure<CartResponse>> response2=restTemplate.exchange("http://localhost:"+port+"/cart/addToCart", HttpMethod.POST, httpEntity2, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});
        assertEquals(HttpStatus.OK,response2.getStatusCode());

        //Adding one address for user
        AddressRequest addressRequest = AddressRequest.builder()
                .streetName("Baner")
                .city("Pune")
                .pinCode(414004)
                .state("Maharastra").build();
        HttpEntity<Object> httpEntity3 = new HttpEntity<>(addressRequest,httpHeaders);
        ResponseEntity<ResponseStructure<AddressResponse>> response3 = restTemplate.exchange( "http://localhost:"+port+"/address/addAddress", HttpMethod.POST, httpEntity3,
                new ParameterizedTypeReference<ResponseStructure<AddressResponse>>() {});
        assertEquals(HttpStatus.CREATED,response3.getStatusCode());
        Long addressId=response3.getBody().getData().getAddressId();


        //Checking everything if user is holding or not
        User user=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(2,user.getCarts().size());
        assertEquals(1,user.getAddresses().size());


        //Placing an order with added address
        OrderRequest orderRequest = OrderRequest.builder().addressId(addressId).build();
        HttpEntity<Object> httpEntity4=new HttpEntity<>(orderRequest,httpHeaders);
        ResponseEntity<ResponseStructure<OrderResponse>> response4=restTemplate.exchange(baseUrl + "/placeOrder", HttpMethod.POST, httpEntity4, new ParameterizedTypeReference<ResponseStructure<OrderResponse>>() {});
        assertEquals(HttpStatus.CREATED,response4.getStatusCode());
        assertEquals("Baner",response4.getBody().getData().getOrderAddress().getStreetName());
        assertEquals(false,response4.getBody().getData().getCancelOrder());
        assertEquals(2,response4.getBody().getData().getOrderBooks().size(),"Checking the quantity of books ordered");

        Long orderId=response4.getBody().getData().getOrderId();


        //This test
        HttpEntity<Object> httpEntity5=new HttpEntity<>(httpHeaders);
        ResponseEntity<ResponseStructure<OrderResponse>> response5=restTemplate.exchange(baseUrl + "/getOrder?orderId="+orderId, HttpMethod.GET, httpEntity5, new ParameterizedTypeReference<ResponseStructure<OrderResponse>>() {});
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(2,response5.getBody().getData().getOrderBooks().size(),"Checking the correct quantity of books and debugging the book details");
        assertEquals(orderId,response5.getBody().getData().getOrderId());
        assertEquals(2,response5.getBody().getData().getOrderQuantity());
        assertFalse(response5.getBody().getData().getCancelOrder());

        User user1=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(0,user1.getCarts().size(),"user cart should get cleared after placing an order successfully");
        assertEquals(1,user1.getAddresses().size());
        assertEquals(1,user1.getOrders().size(),"user should hold one order now");
    }


    @Test
    void getOrderIfOrderIdIsInvalid()
    {
        String authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        CartRequest cartRequest = CartRequest.builder().bookId(2L).build();
        HttpEntity<Object> httpEntity=new HttpEntity<>(cartRequest,httpHeaders);
        ResponseEntity<ResponseStructure<CartResponse>> response=restTemplate.exchange(  "http://localhost:"+port+"/cart/addToCart", HttpMethod.POST, httpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());
        assertEquals(2,response.getBody().getData().getBookId());
        assertEquals(1,response.getBody().getData().getCartQuantity());

        CartRequest cartRequest2 = CartRequest.builder().bookId(1L).build();
        HttpEntity<Object> httpEntity2=new HttpEntity<>(cartRequest2,httpHeaders);
        ResponseEntity<ResponseStructure<CartResponse>> response2=restTemplate.exchange("http://localhost:"+port+"/cart/addToCart", HttpMethod.POST, httpEntity2, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});
        assertEquals(HttpStatus.OK,response2.getStatusCode());

        //Adding one address for user
        AddressRequest addressRequest = AddressRequest.builder()
                .streetName("Baner")
                .city("Pune")
                .pinCode(414004)
                .state("Maharastra").build();
        HttpEntity<Object> httpEntity3 = new HttpEntity<>(addressRequest,httpHeaders);
        ResponseEntity<ResponseStructure<AddressResponse>> response3 = restTemplate.exchange( "http://localhost:"+port+"/address/addAddress", HttpMethod.POST, httpEntity3,
                new ParameterizedTypeReference<ResponseStructure<AddressResponse>>() {});
        assertEquals(HttpStatus.CREATED,response3.getStatusCode());
        Long addressId=response3.getBody().getData().getAddressId();


        //Checking everything if user is holding or not
        User user=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(2,user.getCarts().size());
        assertEquals(1,user.getAddresses().size());


        //Placing an order with added address
        OrderRequest orderRequest = OrderRequest.builder().addressId(addressId).build();
        HttpEntity<Object> httpEntity4=new HttpEntity<>(orderRequest,httpHeaders);
        ResponseEntity<ResponseStructure<OrderResponse>> response4=restTemplate.exchange(baseUrl + "/placeOrder", HttpMethod.POST, httpEntity4, new ParameterizedTypeReference<ResponseStructure<OrderResponse>>() {});
        assertEquals(HttpStatus.CREATED,response4.getStatusCode());
        assertEquals("Baner",response4.getBody().getData().getOrderAddress().getStreetName());
        assertEquals(false,response4.getBody().getData().getCancelOrder());
        assertEquals(2,response4.getBody().getData().getOrderBooks().size(),"Checking the quantity of books ordered");


        //This test
        HttpEntity<Object> httpEntity5=new HttpEntity<>(httpHeaders);
        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/getOrder?orderId=10", HttpMethod.GET, httpEntity5, new ParameterizedTypeReference<ResponseStructure<OrderResponse>>() {}));
        assertEquals(HttpStatus.NOT_FOUND,exception.getStatusCode(),"If order id is not found or order does not belong to user");
    }


    @Test
    void getAllOrdersValidTest()
    {
        String authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        CartRequest cartRequest = CartRequest.builder().bookId(3L).build();
        HttpEntity<Object> httpEntity=new HttpEntity<>(cartRequest,httpHeaders);
        ResponseEntity<ResponseStructure<CartResponse>> response=restTemplate.exchange(  "http://localhost:"+port+"/cart/addToCart", HttpMethod.POST, httpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());
        assertEquals(3,response.getBody().getData().getBookId());
        assertEquals(1,response.getBody().getData().getCartQuantity());

        CartRequest cartRequest2 = CartRequest.builder().bookId(1L).build();
        HttpEntity<Object> httpEntity2=new HttpEntity<>(cartRequest2,httpHeaders);
        ResponseEntity<ResponseStructure<CartResponse>> response2=restTemplate.exchange("http://localhost:"+port+"/cart/addToCart", HttpMethod.POST, httpEntity2, new ParameterizedTypeReference<ResponseStructure<CartResponse>>() {});
        assertEquals(HttpStatus.OK,response2.getStatusCode());

        //Adding one address for user
        AddressRequest addressRequest = AddressRequest.builder()
                .streetName("Baner")
                .city("Pune")
                .pinCode(414004)
                .state("Maharastra").build();
        HttpEntity<Object> httpEntity3 = new HttpEntity<>(addressRequest,httpHeaders);
        ResponseEntity<ResponseStructure<AddressResponse>> response3 = restTemplate.exchange( "http://localhost:"+port+"/address/addAddress", HttpMethod.POST, httpEntity3,
                new ParameterizedTypeReference<ResponseStructure<AddressResponse>>() {});
        assertEquals(HttpStatus.CREATED,response3.getStatusCode());
        Long addressId=response3.getBody().getData().getAddressId();


        //Checking everything if user is holding or not
        User user=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(2,user.getCarts().size());
        assertEquals(1,user.getAddresses().size());


        //Placing an order with added address
        OrderRequest orderRequest = OrderRequest.builder().addressId(addressId).build();
        HttpEntity<Object> httpEntity4=new HttpEntity<>(orderRequest,httpHeaders);
        ResponseEntity<ResponseStructure<OrderResponse>> response4=restTemplate.exchange(baseUrl + "/placeOrder", HttpMethod.POST, httpEntity4, new ParameterizedTypeReference<ResponseStructure<OrderResponse>>() {});
        assertEquals(HttpStatus.CREATED,response4.getStatusCode());
        assertEquals("Baner",response4.getBody().getData().getOrderAddress().getStreetName());
        assertEquals(false,response4.getBody().getData().getCancelOrder());
        assertEquals(2,response4.getBody().getData().getOrderBooks().size(),"Checking the quantity of books ordered");


        HttpEntity<Object> httpEntity5=new HttpEntity<>(httpHeaders);
        ResponseEntity<ResponseStructure<List<OrderResponse>>> response5=restTemplate.exchange(baseUrl + "/getAllOrders", HttpMethod.GET, httpEntity5, new ParameterizedTypeReference<ResponseStructure<List<OrderResponse>>>() {});
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(2,response5.getBody().getData().getFirst().getOrderBooks().size(),"Checking the correct quantity of books and debugging the book details");
        assertEquals(1,response5.getBody().getData().getFirst().getOrderId());
        assertEquals(2,response5.getBody().getData().getFirst().getOrderQuantity());
        assertFalse(response5.getBody().getData().getFirst().getCancelOrder());

        User user1=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(0,user1.getCarts().size(),"user cart should get cleared after placing an order successfully");
        assertEquals(1,user1.getAddresses().size());
        assertEquals(1,user1.getOrders().size(),"user should hold one order now");
    }


    @Test
    void getAllOrdersIfOrdersAreEmpty()
    {
        String authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        HttpEntity<Object> httpEntity=new HttpEntity<>(httpHeaders);

        ResponseEntity<ResponseStructure<List<OrderResponse>>> response=restTemplate.exchange(baseUrl + "/getAllOrders", HttpMethod.GET, httpEntity, new ParameterizedTypeReference<ResponseStructure<List<OrderResponse>>>() {});
        assertEquals(HttpStatus.NO_CONTENT,response.getStatusCode());


        User user1=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(0,user1.getCarts().size(),"user carts should be empty");
        assertEquals(0,user1.getAddresses().size(),"user address should be empty");
        assertEquals(0,user1.getOrders().size(),"user should not hold any order now");
    }
}
