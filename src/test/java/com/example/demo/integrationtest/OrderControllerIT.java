package com.example.demo.integrationtest;

import com.example.demo.entity.Book;
import com.example.demo.entity.User;
import com.example.demo.integrationtest.repo.BookH2Repository;
import com.example.demo.integrationtest.repo.CartH2Repository;
import com.example.demo.integrationtest.repo.OrderH2Repository;
import com.example.demo.integrationtest.repo.UserH2Repository;
import com.example.demo.requestdto.*;
import com.example.demo.responsedto.*;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class OrderControllerIT
{
    @LocalServerPort
    private int port;

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
    }

    private String authToken;

    @Autowired
    private UserH2Repository userH2Repository;

    @Autowired
    private BookH2Repository bookH2Repository;

    @Autowired
    private OrderH2Repository orderH2Repository;

    @Autowired
    private CartH2Repository cartH2Repository;

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
    void placeOrder_ValidTest_AlsoTested_IfAddressIsInvalid_IfCartIsEmpty()
    {
        //adding books to cart to place the order
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);
        CartRequestDto cartRequestDto=CartRequestDto.builder().bookId(3L).build();
        HttpEntity<Object> httpEntity=new HttpEntity<>(cartRequestDto,httpHeaders);
        ResponseEntity<ResponseStructure<CartResponseDto>> response=restTemplate.exchange(  "http://localhost:"+port+"/cart/addToCart", HttpMethod.POST, httpEntity, new ParameterizedTypeReference<ResponseStructure<CartResponseDto>>() {});
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());
        assertEquals(3,response.getBody().getData().getBookId());
        assertEquals(1,response.getBody().getData().getCartQuantity());

        CartRequestDto cartRequestDto2=CartRequestDto.builder().bookId(1L).build();
        HttpEntity<Object> httpEntity2=new HttpEntity<>(cartRequestDto2,httpHeaders);
        ResponseEntity<ResponseStructure<CartResponseDto>> response2=restTemplate.exchange("http://localhost:"+port+"/cart/addToCart", HttpMethod.POST, httpEntity2, new ParameterizedTypeReference<ResponseStructure<CartResponseDto>>() {});
        assertEquals(HttpStatus.OK,response2.getStatusCode());

        //Adding one address for user
        AddressRequestDto addressRequestDto=AddressRequestDto.builder()
                .streetName("Baner")
                .city("Pune")
                .pinCode(414004)
                .state("Maharastra").build();
        HttpEntity<Object> httpEntity3 = new HttpEntity<>(addressRequestDto,httpHeaders);
        ResponseEntity<ResponseStructure<AddressResponseDto>> response3 = restTemplate.exchange( "http://localhost:"+port+"/address/addAddress", HttpMethod.POST, httpEntity3,
                new ParameterizedTypeReference<ResponseStructure<AddressResponseDto>>() {});
        assertEquals(HttpStatus.CREATED,response3.getStatusCode());


        //Checking everything if user is holding or not
        User user=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(2,user.getCarts().size());
        assertEquals(1,user.getAddresses().size());


        //Placing an order with added address
        OrderRequestDto orderRequestDto=OrderRequestDto.builder().addressId(1L).build();
        HttpEntity<Object> httpEntity4=new HttpEntity<>(orderRequestDto,httpHeaders);
        ResponseEntity<ResponseStructure<OrderResponseDto>> response4=restTemplate.exchange(baseUrl + "/placeOrder", HttpMethod.POST, httpEntity4, new ParameterizedTypeReference<ResponseStructure<OrderResponseDto>>() {});
        assertEquals(HttpStatus.CREATED,response4.getStatusCode());
        assertEquals("Baner",response4.getBody().getData().getOrderAddress().getStreetName());
        assertEquals(false,response4.getBody().getData().getCancelOrder());


        User user1=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(0,user1.getCarts().size());
        assertEquals(1,user1.getAddresses().size());
        assertEquals(1,user1.getOrder().size());


        //Trying again if cart is empty
        OrderRequestDto orderRequestDto1=OrderRequestDto.builder().addressId(1L).build();
        HttpEntity<Object> httpEntity5=new HttpEntity<>(orderRequestDto1,httpHeaders);
        ResponseEntity<ResponseStructure<OrderResponseDto>> response5=restTemplate.exchange(baseUrl + "/placeOrder", HttpMethod.POST, httpEntity5, new ParameterizedTypeReference<ResponseStructure<OrderResponseDto>>() {});
        assertEquals(HttpStatus.NO_CONTENT,response5.getStatusCode());


        //Test if address id is invalid
        OrderRequestDto orderRequestDto2=OrderRequestDto.builder().addressId(10L).build();
        HttpEntity<Object> httpEntity6=new HttpEntity<>(orderRequestDto2,httpHeaders);
        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/placeOrder", HttpMethod.POST, httpEntity6, new ParameterizedTypeReference<ResponseStructure<OrderResponseDto>>() {}));
        assertEquals(HttpStatus.NOT_FOUND,exception.getStatusCode());
        String responseBody = exception.getResponseBodyAsString();
        assertTrue(responseBody.contains("\"message\":\"Address not found with Id 10\""));
    }


    @Test
    void cancelOrder_ValidTest()
    {
        placeOrder_ValidTest_AlsoTested_IfAddressIsInvalid_IfCartIsEmpty();

        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        HttpEntity<Object> httpEntity=new HttpEntity<>(httpHeaders);
        ResponseEntity<ResponseStructure<String>> response=restTemplate.exchange(baseUrl + "/cancelOrder/1", HttpMethod.DELETE, httpEntity, new ParameterizedTypeReference<ResponseStructure<String>>() {});
        assertEquals(HttpStatus.OK,response.getStatusCode());

        User user1=userH2Repository.findByEmail("dinesh@gmail.com").get();
        assertEquals(0,user1.getCarts().size());
        assertEquals(1,user1.getAddresses().size());
        assertEquals(1,user1.getOrder().size());


        //Testing to cancel order which is already cancelled
        ResponseEntity<ResponseStructure<String>> response2=restTemplate.exchange(baseUrl + "/cancelOrder/1", HttpMethod.DELETE, httpEntity, new ParameterizedTypeReference<ResponseStructure<String>>() {});
        assertEquals(HttpStatus.NO_CONTENT,response2.getStatusCode());
    }


    @Test
    void cancelOrder_IfOrderIdIsInvalid()
    {
        placeOrder_ValidTest_AlsoTested_IfAddressIsInvalid_IfCartIsEmpty();

        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        HttpEntity<Object> httpEntity=new HttpEntity<>(httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/cancelOrder/10", HttpMethod.DELETE, httpEntity, new ParameterizedTypeReference<ResponseStructure<String>>() {}));
        assertEquals(HttpStatus.NOT_FOUND,exception.getStatusCode());
        String responseBody = exception.getResponseBodyAsString();
        assertTrue(responseBody.contains("\"message\":\"Order not found\""));
    }

    @Test
    void cancelOrder_IfTokenIsInvalidInvalid()
    {
        placeOrder_ValidTest_AlsoTested_IfAddressIsInvalid_IfCartIsEmpty();

        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer token");
        HttpEntity<Object> httpEntity=new HttpEntity<>(httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/cancelOrder/1", HttpMethod.DELETE, httpEntity, new ParameterizedTypeReference<ResponseStructure<String>>() {}));
        assertEquals(HttpStatus.UNAUTHORIZED,exception.getStatusCode());
    }
}
