package com.example.demo.integrationtest;

import com.example.demo.entity.Address;
import com.example.demo.entity.User;
import com.example.demo.integrationtest.repo.AddressH2Repository;
import com.example.demo.integrationtest.repo.UserH2Repository;
import com.example.demo.requestdto.AddressRequestDto;
import com.example.demo.requestdto.UserLoginDTO;
import com.example.demo.requestdto.UserRegisterDTO;
import com.example.demo.responsedto.AddressResponseDto;
import com.example.demo.responsedto.LoginResponseDto;
import com.example.demo.responsedto.RegisterResponseDto;
import com.example.demo.util.ResponseStructure;
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
            UserRegisterDTO userRegisterDTO=UserRegisterDTO.builder()
                    .firstName("Test")
                    .lastName("Chandu")
                    .dob(LocalDate.of(2002,8,24))
                    .email("test@gmail.com")
                    .role("USER")
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
            assertEquals("USER",loginResponse.getBody().getData().getRole());
            authToken = loginResponse.getBody().getMessage();
        }
        return authToken;
    }


    @Test
    void addAddress_ValidTest()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        AddressRequestDto addressRequestDto=AddressRequestDto.builder()
                .streetName("Baner")
                .city("Pune")
                .pinCode(414004)
                .state("Maharastra").build();

        HttpEntity<Object> entity = new HttpEntity<>(addressRequestDto,httpHeaders);

        ResponseEntity<ResponseStructure<AddressResponseDto>> response = restTemplate.exchange(baseUrl + "/addAddress", HttpMethod.POST, entity,
                new ParameterizedTypeReference<ResponseStructure<AddressResponseDto>>() {});

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
    void addAddress_TokenInvalidTest()
    {
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer Token");

        AddressRequestDto addressRequestDto=AddressRequestDto.builder()
                .streetName("Baner")
                .city("Pune")
                .pinCode(414004)
                .state("Maharastra").build();

        HttpEntity<Object> entity = new HttpEntity<>(addressRequestDto,httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/addAddress", HttpMethod.POST, entity,
                new ParameterizedTypeReference<ResponseStructure<AddressResponseDto>>() {}));
        assertEquals(HttpStatus.UNAUTHORIZED,exception.getStatusCode());
        exception.printStackTrace();
    }


    @Test
    public void addAddress_IfBodyIsInvalid()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        AddressRequestDto addressRequestDto=AddressRequestDto.builder()
                .city("Pune")
                .pinCode(414004)
                .state("Maharastra").build();

        HttpEntity<Object> entity = new HttpEntity<>(addressRequestDto,httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/addAddress", HttpMethod.POST, entity,
                new ParameterizedTypeReference<ResponseStructure<AddressResponseDto>>() {}));
        assertEquals(HttpStatus.BAD_REQUEST,exception.getStatusCode());
    }


    @Test
    public void addAddress_IfTokenIsInvalid()
    {
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization",authToken);

        AddressRequestDto addressRequestDto=AddressRequestDto.builder()
                .streetName("Baner")
                .city("Pune")
                .pinCode(414004)
                .state("Maharastra").build();

        HttpEntity<Object> entity = new HttpEntity<>(addressRequestDto,httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/addAddress", HttpMethod.POST, entity,
                new ParameterizedTypeReference<ResponseStructure<AddressResponseDto>>() {}));
        assertEquals(HttpStatus.UNAUTHORIZED,exception.getStatusCode());
    }


    @Test
    public void deleteAddress_ValidTest()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        addAddress_ValidTest();
        HttpEntity<Object> entity = new HttpEntity<>(httpHeaders);

        ResponseEntity<ResponseStructure<AddressResponseDto>> response = restTemplate.exchange(baseUrl + "/deleteAddress/1", HttpMethod.DELETE, entity,
                new ParameterizedTypeReference<ResponseStructure<AddressResponseDto>>() {});

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());
        assertEquals(0,addressH2Repository.findAll().size());

        User user=userH2Repository.findByEmail("test@gmail.com").get();
        List<Address> addresses=user.getAddresses();
        assertEquals(0,addresses.size());
    }


    @Test
    public void deleteAddress_IfAddressIdIsInvalid()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        addAddress_ValidTest();
        HttpEntity<Object> entity = new HttpEntity<>(httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/deleteAddress/4", HttpMethod.DELETE, entity,
                new ParameterizedTypeReference<ResponseStructure<AddressResponseDto>>() {}));
        assertEquals(HttpStatus.NOT_FOUND,exception.getStatusCode());
    }

    @Test
    public void deleteAddress_IfTokenIsInvalid()
    {
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization",null);

        addAddress_ValidTest();
        HttpEntity<Object> entity = new HttpEntity<>(httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/deleteAddress/1", HttpMethod.DELETE, entity,
                new ParameterizedTypeReference<ResponseStructure<AddressResponseDto>>() {}));
        assertEquals(HttpStatus.UNAUTHORIZED,exception.getStatusCode());
    }


    @Test
    public void getAddressById_ValidTest()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        addAddress_ValidTest();
        HttpEntity<Object> entity = new HttpEntity<>(httpHeaders);

        ResponseEntity<ResponseStructure<AddressResponseDto>> response = restTemplate.exchange(baseUrl + "/getAddress/1", HttpMethod.GET, entity,
                new ParameterizedTypeReference<ResponseStructure<AddressResponseDto>>() {});

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
    public void getAddressById_IfAddressIdInvalid()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        addAddress_ValidTest();
        HttpEntity<Object> entity = new HttpEntity<>(httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/getAddress/10", HttpMethod.GET, entity,
                new ParameterizedTypeReference<ResponseStructure<AddressResponseDto>>() {}));
        assertEquals(HttpStatus.NOT_FOUND,exception.getStatusCode());
    }

    @Test
    public void getAddressById_IfTokenIsInvalid()
    {
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization",null);

        addAddress_ValidTest();
        HttpEntity<Object> entity = new HttpEntity<>(httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/getAddress/1", HttpMethod.GET, entity,
                new ParameterizedTypeReference<ResponseStructure<AddressResponseDto>>() {}));
        assertEquals(HttpStatus.UNAUTHORIZED,exception.getStatusCode());
    }


    @Test
    public void addAddress_ValidTest_Second()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        AddressRequestDto addressRequestDto=AddressRequestDto.builder()
                .streetName("BTM")
                .city("Bangalore")
                .pinCode(417152)
                .state("Karnataka").build();

        HttpEntity<Object> entity = new HttpEntity<>(addressRequestDto,httpHeaders);

        ResponseEntity<ResponseStructure<AddressResponseDto>> response = restTemplate.exchange(baseUrl + "/addAddress", HttpMethod.POST, entity,
                new ParameterizedTypeReference<ResponseStructure<AddressResponseDto>>() {});

        assertEquals(HttpStatus.CREATED,response.getStatusCode());
        assertEquals(HttpStatus.CREATED.value(),response.getBody().getStatus());
        assertEquals("BTM",response.getBody().getData().getStreetName());
        assertEquals(417152,addressH2Repository.findById(response.getBody().getData().getAddressId()).get().getPinCode());

        User user=userH2Repository.findByEmail("test@gmail.com").get();
        List<Address> addresses=user.getAddresses();
    }


    @Test
    public void getAllAddress_ValidTest()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        addAddress_ValidTest();
        addAddress_ValidTest_Second();

        HttpEntity<Object> entity = new HttpEntity<>(httpHeaders);

        ResponseEntity<ResponseStructure<List<AddressResponseDto>>> response = restTemplate.exchange(baseUrl + "/getAllAddress", HttpMethod.GET, entity,
                new ParameterizedTypeReference<ResponseStructure<List<AddressResponseDto>>>() {});

        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());
        assertEquals(2,addressH2Repository.findAll().size());

        User user=userH2Repository.findByEmail("test@gmail.com").get();
        List<Address> addresses=user.getAddresses();
        assertEquals(2,addresses.size());
    }


    @Test
    public void getAllAddress_IfTokenIsNotValid()
    {
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization",null);

        addAddress_ValidTest();
        addAddress_ValidTest_Second();

        HttpEntity<Object> entity = new HttpEntity<>(httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/getAllAddress", HttpMethod.GET, entity,
                new ParameterizedTypeReference<ResponseStructure<List<AddressResponseDto>>>() {}));
        assertEquals(HttpStatus.UNAUTHORIZED,exception.getStatusCode());
    }


    @Test
    public void editAddress_ValidTest()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        addAddress_ValidTest();

        AddressRequestDto addressRequestDto=AddressRequestDto.builder()
                .streetName("Ambedgoan")
                .city("Pune")
                .pinCode(414074)
                .state("Maharastra").build();

        HttpEntity<Object> entity = new HttpEntity<>(addressRequestDto,httpHeaders);

        ResponseEntity<ResponseStructure<AddressResponseDto>> response = restTemplate.exchange(baseUrl + "/editAddress/1", HttpMethod.PUT, entity,
                new ParameterizedTypeReference<ResponseStructure<AddressResponseDto>>() {});

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
    public void editAddress_IfAddressIdIsInvalid()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        addAddress_ValidTest();

        AddressRequestDto addressRequestDto=AddressRequestDto.builder()
                .streetName("Ambedgoan")
                .city("Pune")
                .pinCode(414074)
                .state("Maharastra").build();

        HttpEntity<Object> entity = new HttpEntity<>(addressRequestDto,httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/editAddress/10", HttpMethod.PUT, entity,
                new ParameterizedTypeReference<ResponseStructure<AddressResponseDto>>() {}));
        assertEquals(HttpStatus.NOT_FOUND,exception.getStatusCode());
    }


    @Test
    public void editAddress_IfBodyIsInvalid()
    {
        authToken=getAuthToken();
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization","Bearer "+authToken);

        addAddress_ValidTest();

        AddressRequestDto addressRequestDto=AddressRequestDto.builder()
                .city("Pune")
                .pinCode(414074)
                .state("Maharastra").build();

        HttpEntity<Object> entity = new HttpEntity<>(addressRequestDto,httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/editAddress/1", HttpMethod.PUT, entity,
                new ParameterizedTypeReference<ResponseStructure<AddressResponseDto>>() {}));
        assertEquals(HttpStatus.BAD_REQUEST,exception.getStatusCode());
    }


    @Test
    public void editAddress_IfTokenIsInvalid()
    {
        HttpHeaders httpHeaders=new HttpHeaders();
        httpHeaders.set("Authorization",null);

        addAddress_ValidTest();

        AddressRequestDto addressRequestDto=AddressRequestDto.builder()
                .streetName("Baner")
                .city("Pune")
                .pinCode(414074)
                .state("Maharastra").build();

        HttpEntity<Object> entity = new HttpEntity<>(addressRequestDto,httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/editAddress/1", HttpMethod.PUT, entity,
                new ParameterizedTypeReference<ResponseStructure<AddressResponseDto>>() {}));
        assertEquals(HttpStatus.UNAUTHORIZED,exception.getStatusCode());
    }
}
