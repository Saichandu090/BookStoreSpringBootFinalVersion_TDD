package com.example.demo.integrationtest;

import com.example.demo.requestdto.UserLoginDTO;
import com.example.demo.requestdto.UserRegisterDTO;
import com.example.demo.responsedto.LoginResponseDto;
import com.example.demo.responsedto.RegisterResponse;
import com.example.demo.util.ResponseStructure;
import jakarta.transaction.Transactional;
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

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerIT
{
    @LocalServerPort
    private int port;

    private String baseUrl="http://localhost";

    private static RestTemplate restTemplate;

    @Autowired
    private TestH2Repository h2Repository;

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

        ResponseEntity<ResponseStructure<RegisterResponse>> registerResponse = restTemplate.exchange(baseUrl + "/register", HttpMethod.POST, new HttpEntity<>(userRegisterDTO), new ParameterizedTypeReference<ResponseStructure<RegisterResponse>>(){});

        assertEquals(userRegisterDTO.getEmail(),registerResponse.getBody().getData().getEmail());
        assertEquals(userRegisterDTO.getRole(),registerResponse.getBody().getData().getRole());
        assertEquals(1,h2Repository.findAll().size());
    }

    @Test
    public void registerUserTest_WithInvalidBody()
    {
        UserRegisterDTO userRegisterDTO=UserRegisterDTO.builder()
                .firstName("Te")
                .lastName("Chandu")
                .dob(LocalDate.of(2012,8,24))
                .email("test@gmail.com")
                .role("ADMIN")
                .password("saichandu@090").build();

        try {
            ResponseEntity<ResponseStructure<RegisterResponse>> registerResponse = restTemplate.exchange(baseUrl + "/register", HttpMethod.POST, new HttpEntity<>(userRegisterDTO), new ParameterizedTypeReference<ResponseStructure<RegisterResponse>>() {});
            assertEquals(userRegisterDTO.getEmail(), registerResponse.getBody().getData().getEmail());
            assertEquals(userRegisterDTO.getRole(), registerResponse.getBody().getData().getRole());
            assertEquals(1, h2Repository.findAll().size());
        }
        catch (HttpClientErrorException exception)
        {
            assertEquals(HttpStatus.UNAUTHORIZED,exception.getStatusCode());
            exception.printStackTrace();
        }
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

        ResponseEntity<ResponseStructure<RegisterResponse>> registerResponse = restTemplate.exchange(baseUrl + "/register", HttpMethod.POST, new HttpEntity<>(userRegisterDTO), new ParameterizedTypeReference<ResponseStructure<RegisterResponse>>(){});

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

        ResponseEntity<ResponseStructure<RegisterResponse>> registerResponse = restTemplate.exchange(baseUrl + "/register", HttpMethod.POST, new HttpEntity<>(userRegisterDTO), new ParameterizedTypeReference<ResponseStructure<RegisterResponse>>(){});

        assertEquals(HttpStatus.CREATED,registerResponse.getStatusCode());
        assertEquals(HttpStatus.CREATED.value(),registerResponse.getBody().getStatus());
        assertEquals(userRegisterDTO.getEmail(),registerResponse.getBody().getData().getEmail());
        assertEquals(userRegisterDTO.getRole(),registerResponse.getBody().getData().getRole());
        assertEquals(1,h2Repository.findAll().size());


        //Login test

        UserLoginDTO userLoginDTO=UserLoginDTO.builder()
                .email("tes@gmail.com")
                .password("saichandu@090").build();

        try {
            ResponseEntity<ResponseStructure<LoginResponseDto>> loginResponse = restTemplate.exchange(baseUrl + "/login", HttpMethod.POST, new HttpEntity<>(userLoginDTO), new ParameterizedTypeReference<ResponseStructure<LoginResponseDto>>() {
            });

            assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
            assertEquals(HttpStatus.OK.value(), loginResponse.getBody().getStatus());
            assertEquals("test@gmail.com",loginResponse.getBody().getData().getEmail());
            assertEquals("ADMIN",loginResponse.getBody().getData().getRole());
        }
        catch (HttpClientErrorException exception)
        {
            assertEquals(HttpStatus.NOT_FOUND,exception.getStatusCode());
            exception.printStackTrace();
        }
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

        ResponseEntity<ResponseStructure<RegisterResponse>> registerResponse = restTemplate.exchange(baseUrl + "/register", HttpMethod.POST, new HttpEntity<>(userRegisterDTO), new ParameterizedTypeReference<ResponseStructure<RegisterResponse>>(){});

        assertEquals(HttpStatus.CREATED,registerResponse.getStatusCode());
        assertEquals(HttpStatus.CREATED.value(),registerResponse.getBody().getStatus());
        assertEquals(userRegisterDTO.getEmail(),registerResponse.getBody().getData().getEmail());
        assertEquals(userRegisterDTO.getRole(),registerResponse.getBody().getData().getRole());
        assertEquals(1,h2Repository.findAll().size());


        //Login test

        UserLoginDTO userLoginDTO=UserLoginDTO.builder()
                .email("test@gmail.com")
                .password("sai@090").build();

        try {
            ResponseEntity<ResponseStructure<LoginResponseDto>> loginResponse = restTemplate.exchange(baseUrl + "/login", HttpMethod.POST, new HttpEntity<>(userLoginDTO), new ParameterizedTypeReference<ResponseStructure<LoginResponseDto>>() {
            });

            assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
            assertEquals(HttpStatus.OK.value(), loginResponse.getBody().getStatus());
            assertEquals("test@gmail.com",loginResponse.getBody().getData().getEmail());
            assertEquals("ADMIN",loginResponse.getBody().getData().getRole());
        }
        catch (HttpClientErrorException exception)
        {
            assertEquals(HttpStatus.UNAUTHORIZED,exception.getStatusCode());
            exception.printStackTrace();
        }
    }
}
