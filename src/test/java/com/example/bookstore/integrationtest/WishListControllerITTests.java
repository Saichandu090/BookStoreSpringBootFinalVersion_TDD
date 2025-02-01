package com.example.bookstore.integrationtest;

import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.User;
import com.example.bookstore.entity.WishList;
import com.example.bookstore.integrationtest.h2repo.BookH2Repository;
import com.example.bookstore.integrationtest.h2repo.UserH2Repository;
import com.example.bookstore.integrationtest.h2repo.WishListH2Repository;
import com.example.bookstore.requestdto.UserLoginEntity;
import com.example.bookstore.requestdto.UserRegisterEntity;
import com.example.bookstore.requestdto.WishListRequest;
import com.example.bookstore.responsedto.LoginResponse;
import com.example.bookstore.responsedto.RegisterResponse;
import com.example.bookstore.responsedto.WishListResponse;
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
import org.springframework.web.client.RestTemplate;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class WishListControllerITTests
{
    @LocalServerPort
    private int port;

    private String userAuthToken;

    @Autowired
    private WishListH2Repository wishListH2Repository;

    @Autowired
    private BookH2Repository bookH2Repository;

    @Autowired
    private UserH2Repository userH2Repository;

    private String baseUrl="http://localhost";

    private static RestTemplate restTemplate;

    @BeforeAll
    public static void setUp()
    {
        restTemplate=new RestTemplate();
    }

    @BeforeEach
    public void init()
    {
        baseUrl=baseUrl.concat(":").concat(port+"").concat("/wishlist");
        wishListH2Repository.deleteAll();
        userH2Repository.deleteAll();
        bookH2Repository.deleteAll();
    }

    @AfterEach
    public void tearDown()
    {
        wishListH2Repository.deleteAll();
        userH2Repository.deleteAll();
        bookH2Repository.deleteAll();
    }

    protected String getUserAuthToken()
    {
        if (userAuthToken == null)
        {
            UserRegisterEntity userRegisterEntity = UserRegisterEntity.builder()
                    .firstName("Ganesh")
                    .lastName("Chatterge")
                    .dob(LocalDate.of(1995,8,24))
                    .email("ganesh@gmail.com")
                    .role("USER")
                    .password("Ganesh@090").build();

            ResponseEntity<ResponseStructure<RegisterResponse>> registerResponse = restTemplate.exchange( "http://localhost:"+port+"/register", HttpMethod.POST, new HttpEntity<>(userRegisterEntity), new ParameterizedTypeReference<ResponseStructure<RegisterResponse>>(){});

            assertEquals(HttpStatus.CREATED,registerResponse.getStatusCode());
            assertEquals(HttpStatus.CREATED.value(),registerResponse.getBody().getStatus());

            UserLoginEntity userLoginEntity = UserLoginEntity.builder()
                    .email("ganesh@gmail.com")
                    .password("Ganesh@090").build();

            ResponseEntity<ResponseStructure<LoginResponse>> loginResponse = restTemplate.exchange(  "http://localhost:"+port+"/login", HttpMethod.POST, new HttpEntity<>(userLoginEntity), new ParameterizedTypeReference<ResponseStructure<LoginResponse>>(){});

            assertEquals(HttpStatus.OK,loginResponse.getStatusCode());
            assertEquals(HttpStatus.OK.value(),loginResponse.getBody().getStatus());
            assertEquals("ganesh@gmail.com",loginResponse.getBody().getData().getEmail());
            assertEquals("USER",loginResponse.getBody().getData().getRole());
            userAuthToken = loginResponse.getBody().getMessage();
        }
        return userAuthToken;
    }


    @Test
    void addToWishListValidTestIfBookIsNotPresentItShouldAddToUserWishList()
    {
        Book book1=Book.builder()
                .bookName("TEST")
                .bookPrice(199.3)
                .bookAuthor("Chandu")
                .bookDescription("Atom")
                .bookQuantity(78)
                .bookLogo("URL").build();

        Book book2=Book.builder()
                .bookName("Habits")
                .bookPrice(249.49)
                .bookAuthor("Zak crawly")
                .bookDescription("Cricket")
                .bookQuantity(169)
                .bookLogo("URL").build();
        Book saved1=bookH2Repository.save(book1);
        Book saved2=bookH2Repository.save(book2);

        userAuthToken= getUserAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+userAuthToken);
        WishListRequest requestDto= WishListRequest.builder().bookId(saved1.getBookId()).build();
        HttpEntity<Object> httpEntity=new HttpEntity<>(requestDto,httpHeaders);

        ResponseEntity<ResponseStructure<WishListResponse>> response=restTemplate.exchange(baseUrl + "/addToWishList", HttpMethod.POST, httpEntity, new ParameterizedTypeReference<ResponseStructure<WishListResponse>>() {});

        assertEquals(HttpStatus.CREATED,response.getStatusCode());
        assertEquals(saved1.getBookId(),response.getBody().getData().getBookId());

        WishListRequest requestDto2= WishListRequest.builder().bookId(saved2.getBookId()).build();
        HttpEntity<Object> httpEntity2=new HttpEntity<>(requestDto2,httpHeaders);

        ResponseEntity<ResponseStructure<WishListResponse>> response2=restTemplate.exchange(baseUrl + "/addToWishList", HttpMethod.POST, httpEntity2, new ParameterizedTypeReference<ResponseStructure<WishListResponse>>() {});

        assertEquals(HttpStatus.CREATED,response2.getStatusCode());
        assertEquals(saved2.getBookId(),response2.getBody().getData().getBookId());

        User user=userH2Repository.findByEmail("ganesh@gmail.com").get();
        List<WishList> wishLists=user.getWishList();
        assertEquals(2,wishLists.size());
    }


    @Test
    void addToWishListValidTestIfBookIsAlreadyPresentItShouldRemoveFromUserWishList()
    {
        Book book1=Book.builder()
                .bookName("TEST")
                .bookPrice(199.3)
                .bookAuthor("Chandu")
                .bookDescription("Atom")
                .bookQuantity(78)
                .bookLogo("URL").build();

        Book saved1=bookH2Repository.save(book1);

        userAuthToken= getUserAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+userAuthToken);
        WishListRequest requestDto= WishListRequest.builder().bookId(saved1.getBookId()).build();
        HttpEntity<Object> httpEntity=new HttpEntity<>(requestDto,httpHeaders);

        ResponseEntity<ResponseStructure<WishListResponse>> response=restTemplate.exchange(baseUrl + "/addToWishList", HttpMethod.POST, httpEntity, new ParameterizedTypeReference<ResponseStructure<WishListResponse>>() {});

        assertEquals(HttpStatus.CREATED,response.getStatusCode());
        assertEquals(saved1.getBookId(),response.getBody().getData().getBookId());
        User user1=userH2Repository.findByEmail("ganesh@gmail.com").get();
        List<WishList> wishLists1=user1.getWishList();
        assertEquals(1,wishLists1.size());


        WishListRequest requestDto2= WishListRequest.builder().bookId(saved1.getBookId()).build();
        HttpEntity<Object> httpEntity2=new HttpEntity<>(requestDto2,httpHeaders);

        ResponseEntity<ResponseStructure<WishListResponse>> response2=restTemplate.exchange(baseUrl + "/addToWishList", HttpMethod.POST, httpEntity2, new ParameterizedTypeReference<ResponseStructure<WishListResponse>>() {});

        assertEquals(HttpStatus.OK,response2.getStatusCode());
        assertEquals("Book "+saved1.getBookName()+" has successfully removed from wishlist successfully",response2.getBody().getMessage());

        User user=userH2Repository.findByEmail("ganesh@gmail.com").get();
        List<WishList> wishLists=user.getWishList();
        assertEquals(0,wishLists.size());


        WishListRequest requestDto3= WishListRequest.builder().bookId(saved1.getBookId()).build();
        HttpEntity<Object> httpEntity3=new HttpEntity<>(requestDto3,httpHeaders);

        ResponseEntity<ResponseStructure<WishListResponse>> response3=restTemplate.exchange(baseUrl + "/addToWishList", HttpMethod.POST, httpEntity3, new ParameterizedTypeReference<ResponseStructure<WishListResponse>>() {});

        assertEquals(HttpStatus.CREATED,response3.getStatusCode());
        assertEquals(saved1.getBookId(),response3.getBody().getData().getBookId());
        User user3=userH2Repository.findByEmail("ganesh@gmail.com").get();
        List<WishList> wishLists3=user3.getWishList();
        assertEquals(1,wishLists3.size());
    }



    @Test
    void getWishListValidTest()
    {
        Book book1=Book.builder()
                .bookName("TEST")
                .bookPrice(199.3)
                .bookAuthor("Chandu")
                .bookDescription("Atom")
                .bookQuantity(78)
                .bookLogo("URL").build();

        Book book2=Book.builder()
                .bookName("Habits")
                .bookPrice(249.49)
                .bookAuthor("Zak crawly")
                .bookDescription("Cricket")
                .bookQuantity(169)
                .bookLogo("URL").build();
        Book saved1=bookH2Repository.save(book1);
        Book saved2=bookH2Repository.save(book2);

        userAuthToken= getUserAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+userAuthToken);
        WishListRequest requestDto= WishListRequest.builder().bookId(saved1.getBookId()).build();

        HttpEntity<Object> httpEntity=new HttpEntity<>(requestDto,httpHeaders);
        ResponseEntity<ResponseStructure<WishListResponse>> response=restTemplate.exchange(baseUrl + "/addToWishList", HttpMethod.POST, httpEntity, new ParameterizedTypeReference<ResponseStructure<WishListResponse>>() {});
        assertEquals(HttpStatus.CREATED,response.getStatusCode());
        assertEquals(saved1.getBookId(),response.getBody().getData().getBookId());
        User user1=userH2Repository.findByEmail("ganesh@gmail.com").get();
        List<WishList> wishLists1=user1.getWishList();
        assertEquals(1,wishLists1.size());

        WishListRequest requestDto2= WishListRequest.builder().bookId(saved2.getBookId()).build();

        HttpEntity<Object> httpEntity2=new HttpEntity<>(requestDto2,httpHeaders);

        ResponseEntity<ResponseStructure<WishListResponse>> response2=restTemplate.exchange(baseUrl + "/addToWishList", HttpMethod.POST, httpEntity2, new ParameterizedTypeReference<ResponseStructure<WishListResponse>>() {});
        assertEquals(HttpStatus.CREATED,response2.getStatusCode());
        assertEquals(saved2.getBookId(),response2.getBody().getData().getBookId());
        User user2=userH2Repository.findByEmail("ganesh@gmail.com").get();
        List<WishList> wishLists2=user2.getWishList();
        assertEquals(2,wishLists2.size());


        //Fetching the wishlist
        HttpEntity<Object> httpEntity3=new HttpEntity<>(httpHeaders);

        ResponseEntity<ResponseStructure<List<WishListResponse>>> response3=restTemplate.exchange(baseUrl + "/getWishList", HttpMethod.GET, httpEntity3, new ParameterizedTypeReference<ResponseStructure<List<WishListResponse>>>() {});
        assertEquals(HttpStatus.OK,response3.getStatusCode());
        assertEquals(2,response3.getBody().getData().size());


        // Removing 1 from wishlist
        WishListRequest requestDto4= WishListRequest.builder().bookId(saved2.getBookId()).build();
        HttpEntity<Object> httpEntity4=new HttpEntity<>(requestDto4,httpHeaders);

        ResponseEntity<ResponseStructure<WishListResponse>> response4=restTemplate.exchange(baseUrl + "/addToWishList", HttpMethod.POST, httpEntity4, new ParameterizedTypeReference<ResponseStructure<WishListResponse>>() {});
        assertEquals(HttpStatus.OK,response4.getStatusCode());
        assertEquals(saved2.getBookId(),response2.getBody().getData().getBookId());
        User user4=userH2Repository.findByEmail("ganesh@gmail.com").get();
        List<WishList> wishLists4=user4.getWishList();
        assertEquals(1,wishLists4.size());


        //Fetching user wishlist again to see the difference
        HttpEntity<Object> httpEntity5=new HttpEntity<>(httpHeaders);

        ResponseEntity<ResponseStructure<List<WishListResponse>>> response5=restTemplate.exchange(baseUrl + "/getWishList", HttpMethod.GET, httpEntity5, new ParameterizedTypeReference<ResponseStructure<List<WishListResponse>>>() {});
        assertEquals(HttpStatus.OK,response5.getStatusCode());
        assertEquals(1,response5.getBody().getData().size());

        User user=userH2Repository.findByEmail("ganesh@gmail.com").get();
        assertEquals(1,user.getWishList().size());
    }


    @Test
    void getWishListIfWishListIsEmpty()
    {
        userAuthToken= getUserAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+userAuthToken);

        HttpEntity<Object> httpEntity=new HttpEntity<>(httpHeaders);
        ResponseEntity<ResponseStructure<List<WishListResponse>>> response=restTemplate.exchange(baseUrl + "/getWishList", HttpMethod.GET, httpEntity, new ParameterizedTypeReference<ResponseStructure<List<WishListResponse>>>() {});
        assertEquals(HttpStatus.NO_CONTENT,response.getStatusCode());
    }


    @Test
    void isInWishListTest()
    {
        Book book1=Book.builder()
                .bookName("TEST")
                .bookPrice(199.3)
                .bookAuthor("Chandu")
                .bookDescription("Atom")
                .bookQuantity(78)
                .bookLogo("URL").build();

        Book book2=Book.builder()
                .bookName("Habits")
                .bookPrice(249.49)
                .bookAuthor("Zak crawly")
                .bookDescription("Cricket")
                .bookQuantity(169)
                .bookLogo("URL").build();
        Book saved1=bookH2Repository.save(book1);
        Book saved2=bookH2Repository.save(book2);

        userAuthToken= getUserAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+userAuthToken);
        WishListRequest requestDto= WishListRequest.builder().bookId(saved1.getBookId()).build();

        HttpEntity<Object> httpEntity=new HttpEntity<>(requestDto,httpHeaders);
        ResponseEntity<ResponseStructure<WishListResponse>> response=restTemplate.exchange(baseUrl + "/addToWishList", HttpMethod.POST, httpEntity, new ParameterizedTypeReference<ResponseStructure<WishListResponse>>() {});
        assertEquals(HttpStatus.CREATED,response.getStatusCode());
        assertEquals(saved1.getBookId(),response.getBody().getData().getBookId());
        User user1=userH2Repository.findByEmail("ganesh@gmail.com").get();
        List<WishList> wishLists1=user1.getWishList();
        assertEquals(1,wishLists1.size());

        WishListRequest requestDto2= WishListRequest.builder().bookId(saved2.getBookId()).build();

        HttpEntity<Object> httpEntity2=new HttpEntity<>(requestDto2,httpHeaders);

        ResponseEntity<ResponseStructure<WishListResponse>> response2=restTemplate.exchange(baseUrl + "/addToWishList", HttpMethod.POST, httpEntity2, new ParameterizedTypeReference<ResponseStructure<WishListResponse>>() {});
        assertEquals(HttpStatus.CREATED,response2.getStatusCode());
        assertEquals(saved2.getBookId(),response2.getBody().getData().getBookId());
        User user2=userH2Repository.findByEmail("ganesh@gmail.com").get();
        List<WishList> wishLists2=user2.getWishList();
        assertEquals(2,wishLists2.size());


        //Checking the wishlist
        HttpEntity<Object> httpEntity3=new HttpEntity<>(httpHeaders);
        ResponseEntity<ResponseStructure<Boolean>> response3=restTemplate.exchange(baseUrl + "/isInWishList/2", HttpMethod.GET, httpEntity3, new ParameterizedTypeReference<ResponseStructure<Boolean>>() {});
        assertEquals(HttpStatus.OK,response3.getStatusCode());
    }

}
