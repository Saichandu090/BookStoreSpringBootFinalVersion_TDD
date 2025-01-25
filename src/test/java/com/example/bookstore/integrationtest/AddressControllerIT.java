package com.example.bookstore.integrationtest;

import com.example.bookstore.entity.Address;
import com.example.bookstore.entity.User;
import com.example.bookstore.integrationtest.h2repo.AddressH2Repository;
import com.example.bookstore.integrationtest.h2repo.UserH2Repository;
import com.example.bookstore.requestdto.AddressRequest;
import com.example.bookstore.requestdto.UserLoginEntity;
import com.example.bookstore.requestdto.UserRegisterEntity;
import com.example.bookstore.responsedto.AddressResponse;
import com.example.bookstore.responsedto.LoginResponse;
import com.example.bookstore.responsedto.RegisterResponse;
import com.example.bookstore.util.ResponseStructure;
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
public class AddressControllerIT
{
    @LocalServerPort
    private int port;

    private String authToken;

    private String baseUrl="http://localhost";

    private static RestTemplate restTemplate;

    @Autowired
    private AddressH2Repository addressH2Repository;

    @Autowired
    private UserH2Repository userH2Repository;

    @BeforeAll
    public static void init()
    {
        restTemplate=new RestTemplate();
    }

    @BeforeEach
    public void setUp()
    {
        baseUrl=baseUrl.concat(":").concat(port+"").concat("/address");
    }

    protected String getAuthToken()
    {
        if (authToken == null)
        {
            UserRegisterEntity userRegisterEntity = UserRegisterEntity.builder()
                    .firstName("Test")
                    .lastName("Chandu")
                    .dob(LocalDate.of(2002,8,24))
                    .email("test@gmail.com")
                    .role("USER")
                    .password("Saichandu@090").build();

            ResponseEntity<ResponseStructure<RegisterResponse>> registerResponse = restTemplate.exchange( "http://localhost:"+port+"/register", HttpMethod.POST, new HttpEntity<>(userRegisterEntity), new ParameterizedTypeReference<ResponseStructure<RegisterResponse>>(){});

            assertEquals(HttpStatus.CREATED,registerResponse.getStatusCode());
            assertEquals(HttpStatus.CREATED.value(),registerResponse.getBody().getStatus());

            UserLoginEntity userLoginEntity = UserLoginEntity.builder()
                    .email("test@gmail.com")
                    .password("Saichandu@090").build();

            ResponseEntity<ResponseStructure<LoginResponse>> loginResponse = restTemplate.exchange(  "http://localhost:"+port+"/login", HttpMethod.POST, new HttpEntity<>(userLoginEntity), new ParameterizedTypeReference<ResponseStructure<LoginResponse>>(){});

            assertEquals(HttpStatus.OK,loginResponse.getStatusCode());
            assertEquals(HttpStatus.OK.value(),loginResponse.getBody().getStatus());
            assertEquals("test@gmail.com",loginResponse.getBody().getData().getEmail());
            assertEquals("USER",loginResponse.getBody().getData().getRole());
            authToken = loginResponse.getBody().getMessage();
        }
        return authToken;
    }


    @Test
    void addAddressValidTest()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        AddressRequest addressRequest = AddressRequest.builder()
                .streetName("Baner")
                .city("Pune")
                .pinCode(414004)
                .state("Maharastra").build();

        HttpEntity<Object> entity = new HttpEntity<>(addressRequest,httpHeaders);

        ResponseEntity<ResponseStructure<AddressResponse>> response = restTemplate.exchange(baseUrl + "/addAddress", HttpMethod.POST, entity,
                new ParameterizedTypeReference<ResponseStructure<AddressResponse>>() {});

        assertEquals(HttpStatus.CREATED,response.getStatusCode());
        assertEquals(HttpStatus.CREATED.value(),response.getBody().getStatus());
        assertEquals("Baner",response.getBody().getData().getStreetName());
        assertEquals(414004,addressH2Repository.findById(response.getBody().getData().getAddressId()).get().getPinCode());
        assertEquals(1,addressH2Repository.findAll().size());

        User user=userH2Repository.findByEmail("test@gmail.com").get();
        List<Address> addresses=user.getAddresses();
        assertEquals(addresses.getFirst().getAddressId(),response.getBody().getData().getAddressId());
    }

    @Test
    void addAddressTokenInvalidTest()
    {
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer Token");

        AddressRequest addressRequest = AddressRequest.builder()
                .streetName("Baner")
                .city("Pune")
                .pinCode(414004)
                .state("Maharastra").build();

        HttpEntity<Object> entity = new HttpEntity<>(addressRequest,httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/addAddress", HttpMethod.POST, entity,
                new ParameterizedTypeReference<ResponseStructure<AddressResponse>>() {}));
        assertEquals(HttpStatus.UNAUTHORIZED,exception.getStatusCode());
        exception.printStackTrace();
    }


    @Test
    public void addAddressIfBodyIsInvalid()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        AddressRequest addressRequest = AddressRequest.builder()
                .city("Pune")
                .pinCode(414004)
                .state("Maharastra").build();

        HttpEntity<Object> entity = new HttpEntity<>(addressRequest,httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/addAddress", HttpMethod.POST, entity,
                new ParameterizedTypeReference<ResponseStructure<AddressResponse>>() {}));
        assertEquals(HttpStatus.BAD_REQUEST,exception.getStatusCode());
    }


    @Test
    public void addAddressIfTokenIsInvalid()
    {
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization",authToken);

        AddressRequest addressRequest = AddressRequest.builder()
                .streetName("Baner")
                .city("Pune")
                .pinCode(414004)
                .state("Maharastra").build();

        HttpEntity<Object> entity = new HttpEntity<>(addressRequest,httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/addAddress", HttpMethod.POST, entity,
                new ParameterizedTypeReference<ResponseStructure<AddressResponse>>() {}));
        assertEquals(HttpStatus.UNAUTHORIZED,exception.getStatusCode());
    }


    @Test
    public void deleteAddressValidTest()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        addAddressValidTest();
        HttpEntity<Object> entity = new HttpEntity<>(httpHeaders);

        ResponseEntity<ResponseStructure<AddressResponse>> response = restTemplate.exchange(baseUrl + "/deleteAddress/1", HttpMethod.DELETE, entity,
                new ParameterizedTypeReference<ResponseStructure<AddressResponse>>() {});

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());
        assertEquals(1,addressH2Repository.findAll().size());

        User user=userH2Repository.findByEmail("test@gmail.com").get();
        List<Address> addresses=user.getAddresses();
        assertEquals(0,addresses.size());
    }


    @Test
    public void deleteAddressIfAddressIdIsInvalid()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        addAddressValidTest();
        HttpEntity<Object> entity = new HttpEntity<>(httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/deleteAddress/4", HttpMethod.DELETE, entity,
                new ParameterizedTypeReference<ResponseStructure<AddressResponse>>() {}));
        assertEquals(HttpStatus.NOT_FOUND,exception.getStatusCode());
    }

    @Test
    public void deleteAddressIfTokenIsInvalid()
    {
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization",null);

        addAddressValidTest();
        HttpEntity<Object> entity = new HttpEntity<>(httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/deleteAddress/1", HttpMethod.DELETE, entity,
                new ParameterizedTypeReference<ResponseStructure<AddressResponse>>() {}));
        assertEquals(HttpStatus.UNAUTHORIZED,exception.getStatusCode());
    }


    @Test
    public void getAddressByIdValidTest()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        addAddressValidTest();
        HttpEntity<Object> entity = new HttpEntity<>(httpHeaders);

        ResponseEntity<ResponseStructure<AddressResponse>> response = restTemplate.exchange(baseUrl + "/getAddress/1", HttpMethod.GET, entity,
                new ParameterizedTypeReference<ResponseStructure<AddressResponse>>() {});

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());
        assertEquals(414004,response.getBody().getData().getPinCode());
        assertEquals("Baner",response.getBody().getData().getStreetName());
        assertEquals(1,addressH2Repository.findAll().size());

        User user=userH2Repository.findByEmail("test@gmail.com").get();
        List<Address> addresses=user.getAddresses();
        assertEquals(1,addresses.size());
    }

    @Test
    public void getAddressByIdIfAddressIdInvalid()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        addAddressValidTest();
        HttpEntity<Object> entity = new HttpEntity<>(httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/getAddress/10", HttpMethod.GET, entity,
                new ParameterizedTypeReference<ResponseStructure<AddressResponse>>() {}));
        assertEquals(HttpStatus.NOT_FOUND,exception.getStatusCode());
    }

    @Test
    public void getAddressByIdIfTokenIsInvalid()
    {
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization",null);

        addAddressValidTest();
        HttpEntity<Object> entity = new HttpEntity<>(httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/getAddress/1", HttpMethod.GET, entity,
                new ParameterizedTypeReference<ResponseStructure<AddressResponse>>() {}));
        assertEquals(HttpStatus.UNAUTHORIZED,exception.getStatusCode());
    }


    @Test
    public void addAddressValidTestSecond()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        AddressRequest addressRequest = AddressRequest.builder()
                .streetName("BTM")
                .city("Bangalore")
                .pinCode(417152)
                .state("Karnataka").build();

        HttpEntity<Object> entity = new HttpEntity<>(addressRequest,httpHeaders);

        ResponseEntity<ResponseStructure<AddressResponse>> response = restTemplate.exchange(baseUrl + "/addAddress", HttpMethod.POST, entity,
                new ParameterizedTypeReference<ResponseStructure<AddressResponse>>() {});

        assertEquals(HttpStatus.CREATED,response.getStatusCode());
        assertEquals(HttpStatus.CREATED.value(),response.getBody().getStatus());
        assertEquals("BTM",response.getBody().getData().getStreetName());
        assertEquals(417152,addressH2Repository.findById(response.getBody().getData().getAddressId()).get().getPinCode());

        User user=userH2Repository.findByEmail("test@gmail.com").get();
        List<Address> addresses=user.getAddresses();
    }


    @Test
    public void getAllAddressValidTest()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        addAddressValidTest();
        addAddressValidTestSecond();

        HttpEntity<Object> entity = new HttpEntity<>(httpHeaders);

        ResponseEntity<ResponseStructure<List<AddressResponse>>> response = restTemplate.exchange(baseUrl + "/getAllAddress", HttpMethod.GET, entity,
                new ParameterizedTypeReference<ResponseStructure<List<AddressResponse>>>() {});

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());
        assertEquals(2,addressH2Repository.findAll().size());

        User user=userH2Repository.findByEmail("test@gmail.com").get();
        List<Address> addresses=user.getAddresses();
        assertEquals(2,addresses.size());
    }


    @Test
    public void getAllAddressIfTokenIsNotValid()
    {
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization",null);

        addAddressValidTest();
        addAddressValidTestSecond();

        HttpEntity<Object> entity = new HttpEntity<>(httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/getAllAddress", HttpMethod.GET, entity,
                new ParameterizedTypeReference<ResponseStructure<List<AddressResponse>>>() {}));
        assertEquals(HttpStatus.UNAUTHORIZED,exception.getStatusCode());
    }


    @Test
    public void editAddressValidTest()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        addAddressValidTest();

        AddressRequest addressRequest = AddressRequest.builder()
                .streetName("Ambedgoan")
                .city("Pune")
                .pinCode(414074)
                .state("Maharastra").build();

        HttpEntity<Object> entity = new HttpEntity<>(addressRequest,httpHeaders);

        ResponseEntity<ResponseStructure<AddressResponse>> response = restTemplate.exchange(baseUrl + "/editAddress/1", HttpMethod.PUT, entity,
                new ParameterizedTypeReference<ResponseStructure<AddressResponse>>() {});

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());
        assertEquals("Ambedgoan",response.getBody().getData().getStreetName());
        assertEquals(414074,addressH2Repository.findById(response.getBody().getData().getAddressId()).get().getPinCode());
        assertEquals(1,response.getBody().getData().getAddressId());
        assertEquals(1,addressH2Repository.findAll().size());

        User user=userH2Repository.findByEmail("test@gmail.com").get();
        List<Address> addresses=user.getAddresses();
        assertEquals("Ambedgoan",addresses.getFirst().getStreetName());
    }


    @Test
    public void editAddressIfAddressIdIsInvalid()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        addAddressValidTest();

        AddressRequest addressRequest = AddressRequest.builder()
                .streetName("Ambedgoan")
                .city("Pune")
                .pinCode(414074)
                .state("Maharastra").build();

        HttpEntity<Object> entity = new HttpEntity<>(addressRequest,httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/editAddress/10", HttpMethod.PUT, entity,
                new ParameterizedTypeReference<ResponseStructure<AddressResponse>>() {}));
        assertEquals(HttpStatus.NOT_FOUND,exception.getStatusCode());
    }


    @Test
    public void editAddressIfBodyIsInvalid()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        addAddressValidTest();

        AddressRequest addressRequest = AddressRequest.builder()
                .city("Pune")
                .pinCode(414074)
                .state("Maharastra").build();

        HttpEntity<Object> entity = new HttpEntity<>(addressRequest,httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/editAddress/1", HttpMethod.PUT, entity,
                new ParameterizedTypeReference<ResponseStructure<AddressResponse>>() {}));
        assertEquals(HttpStatus.BAD_REQUEST,exception.getStatusCode());
    }


    @Test
    public void editAddressIfTokenIsInvalid()
    {
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization",null);

        addAddressValidTest();

        AddressRequest addressRequest = AddressRequest.builder()
                .streetName("Baner")
                .city("Pune")
                .pinCode(414074)
                .state("Maharastra").build();

        HttpEntity<Object> entity = new HttpEntity<>(addressRequest,httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/editAddress/1", HttpMethod.PUT, entity,
                new ParameterizedTypeReference<ResponseStructure<AddressResponse>>() {}));
        assertEquals(HttpStatus.UNAUTHORIZED,exception.getStatusCode());
    }
}
