package com.example.demo.integrationtest;

import com.example.demo.integrationtest.repo.UserH2Repository;
import com.example.demo.requestdto.UserLoginDTO;
import com.example.demo.requestdto.UserRegisterDTO;
import com.example.demo.responsedto.LoginResponseDto;
import com.example.demo.responsedto.RegisterResponseDto;
import com.example.demo.util.ResponseStructure;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerIT
{
    @LocalServerPort
    private int port;

    private String baseUrl="http://localhost";

    private static RestTemplate restTemplate;

    @Autowired
    private UserH2Repository h2Repository;

    @BeforeAll
    public static void init()
    {
        restTemplate=new RestTemplate();
    }

    @BeforeEach
    public void setUp()
    {
        baseUrl=baseUrl.concat(":").concat(port+"");
    }

    @Test
    public void registerUserTest()
    {
        UserRegisterDTO userRegisterDTO=UserRegisterDTO.builder()
                .firstName("Test")
                .lastName("Chandu")
                .dob(LocalDate.of(2002,8,24))
                .email("test@gmail.com")
                .role("ADMIN")
                .password("saichandu@090").build();

        ResponseEntity<ResponseStructure<RegisterResponseDto>> registerResponse = restTemplate.exchange(baseUrl + "/register", HttpMethod.POST, new HttpEntity<>(userRegisterDTO), new ParameterizedTypeReference<ResponseStructure<RegisterResponseDto>>(){});

        assertEquals(userRegisterDTO.getEmail(),registerResponse.getBody().getData().getEmail());
        assertEquals(userRegisterDTO.getRole(),registerResponse.getBody().getData().getRole());
        assertEquals(1,h2Repository.findAll().size());
    }


    @Test
    public void registerUserTest_WithInvalidBody()
    {
        UserRegisterDTO requestDTO = new UserRegisterDTO();
        requestDTO.setLastName("Chandu");
        requestDTO.setDob(LocalDate.of(2012, 8, 24));
        requestDTO.setPassword("saichandu@090");
        requestDTO.setEmail("test@gmail.com");
        requestDTO.setRole("ADMIN");

        HttpEntity<Object> httpEntity=new HttpEntity<>(requestDTO);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/register", HttpMethod.POST, httpEntity,
                new ParameterizedTypeReference<ResponseStructure<RegisterResponseDto>>() {}));
        assertEquals(HttpStatus.BAD_REQUEST,exception.getStatusCode());
    }


    @Test
    public void loginTest()
    {
        //Register test

        UserRegisterDTO userRegisterDTO=UserRegisterDTO.builder()
                .firstName("Test")
                .lastName("Chandu")
                .dob(LocalDate.of(2002,8,24))
                .email("test@gmail.com")
                .role("ADMIN")
                .password("saichandu@090").build();

        ResponseEntity<ResponseStructure<RegisterResponseDto>> registerResponse = restTemplate.exchange(baseUrl + "/register", HttpMethod.POST, new HttpEntity<>(userRegisterDTO), new ParameterizedTypeReference<ResponseStructure<RegisterResponseDto>>(){});

        assertEquals(HttpStatus.CREATED,registerResponse.getStatusCode());
        assertEquals(HttpStatus.CREATED.value(),registerResponse.getBody().getStatus());
        assertEquals(userRegisterDTO.getEmail(),registerResponse.getBody().getData().getEmail());
        assertEquals(userRegisterDTO.getRole(),registerResponse.getBody().getData().getRole());
        assertEquals(1,h2Repository.findAll().size());


        //Login test

        UserLoginDTO userLoginDTO=UserLoginDTO.builder()
                .email("test@gmail.com")
                .password("saichandu@090").build();

        ResponseEntity<ResponseStructure<LoginResponseDto>> loginResponse = restTemplate.exchange(baseUrl + "/login", HttpMethod.POST, new HttpEntity<>(userLoginDTO), new ParameterizedTypeReference<ResponseStructure<LoginResponseDto>>(){});

        assertEquals(HttpStatus.OK,loginResponse.getStatusCode());
        assertEquals(HttpStatus.OK.value(),loginResponse.getBody().getStatus());
        assertEquals("test@gmail.com",loginResponse.getBody().getData().getEmail());
        assertEquals("ADMIN",loginResponse.getBody().getData().getRole());
    }

    @Test
    public void loginTest_WithInvalidUserEmail()
    {
        //Register test

        UserRegisterDTO userRegisterDTO=UserRegisterDTO.builder()
                .firstName("Test")
                .lastName("Chandu")
                .dob(LocalDate.of(2002,8,24))
                .email("test@gmail.com")
                .role("ADMIN")
                .password("saichandu@090").build();

        ResponseEntity<ResponseStructure<RegisterResponseDto>> registerResponse = restTemplate.exchange(baseUrl + "/register", HttpMethod.POST, new HttpEntity<>(userRegisterDTO), new ParameterizedTypeReference<ResponseStructure<RegisterResponseDto>>(){});

        assertEquals(HttpStatus.CREATED,registerResponse.getStatusCode());
        assertEquals(HttpStatus.CREATED.value(),registerResponse.getBody().getStatus());
        assertEquals(userRegisterDTO.getEmail(),registerResponse.getBody().getData().getEmail());
        assertEquals(userRegisterDTO.getRole(),registerResponse.getBody().getData().getRole());
        assertEquals(1,h2Repository.findAll().size());


        //Login test

        UserLoginDTO userLoginDTO=UserLoginDTO.builder()
                .email("tes@gmail.com")
                .password("saichandu@090").build();

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()-> restTemplate.exchange(baseUrl + "/login", HttpMethod.POST, new HttpEntity<>(userLoginDTO), new ParameterizedTypeReference<ResponseStructure<LoginResponseDto>>() {
        }));
        assertEquals(HttpStatus.NOT_FOUND,exception.getStatusCode());
    }


    @Test
    public void loginTest_WithInvalidUserPassword()
    {
        //Register test
        UserRegisterDTO userRegisterDTO=UserRegisterDTO.builder()
                .firstName("Test")
                .lastName("Chandu")
                .dob(LocalDate.of(2002,8,24))
                .email("test@gmail.com")
                .role("ADMIN")
                .password("saichandu@090").build();

        ResponseEntity<ResponseStructure<RegisterResponseDto>> registerResponse = restTemplate.exchange(baseUrl + "/register", HttpMethod.POST, new HttpEntity<>(userRegisterDTO), new ParameterizedTypeReference<ResponseStructure<RegisterResponseDto>>(){});

        assertEquals(HttpStatus.CREATED,registerResponse.getStatusCode());
        assertEquals(HttpStatus.CREATED.value(),registerResponse.getBody().getStatus());
        assertEquals(userRegisterDTO.getEmail(),registerResponse.getBody().getData().getEmail());
        assertEquals(userRegisterDTO.getRole(),registerResponse.getBody().getData().getRole());
        assertEquals(1,h2Repository.findAll().size());


        //Login test
        UserLoginDTO userLoginDTO=UserLoginDTO.builder()
                .email("test@gmail.com")
                .password("sai@090").build();

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/login", HttpMethod.POST, new HttpEntity<>(userLoginDTO), new ParameterizedTypeReference<ResponseStructure<LoginResponseDto>>() {
        }));
        assertEquals(HttpStatus.UNAUTHORIZED,exception.getStatusCode());
    }
}
