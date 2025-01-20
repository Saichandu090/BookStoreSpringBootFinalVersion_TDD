package com.example.bookstore.integrationtest;

import com.example.bookstore.entity.User;
import com.example.bookstore.integrationtest.h2repo.UserH2Repository;
import com.example.bookstore.requestdto.UserLoginEntity;
import com.example.bookstore.requestdto.UserRegisterEntity;
import com.example.bookstore.responsedto.LoginResponse;
import com.example.bookstore.responsedto.RegisterResponse;
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

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerIT
{
    @LocalServerPort
    private int port;

    private String baseUrl="http://localhost";

    private static RestTemplate restTemplate;

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
        baseUrl=baseUrl.concat(":").concat(port+"");
    }

    @Test
    void registerUserTest()
    {
        UserRegisterEntity userRegisterEntity = UserRegisterEntity.builder()
                .firstName("Test")
                .lastName("Chandu")
                .dob(LocalDate.of(2002,8,24))
                .email("test@gmail.com")
                .role("ADMIN")
                .password("saichandu@090").build();

        ResponseEntity<ResponseStructure<RegisterResponse>> registerResponse = restTemplate.exchange(baseUrl + "/register", HttpMethod.POST, new HttpEntity<>(userRegisterEntity), new ParameterizedTypeReference<ResponseStructure<RegisterResponse>>(){});

        assertEquals(userRegisterEntity.getEmail(),registerResponse.getBody().getData().getEmail());
        assertEquals(userRegisterEntity.getRole(),registerResponse.getBody().getData().getRole());
        assertEquals(1, userH2Repository.findAll().size());
    }


    @Test
    public void registerUserTestWithInvalidBody()
    {
        UserRegisterEntity requestDTO = new UserRegisterEntity();
        requestDTO.setLastName("Chandu");
        requestDTO.setDob(LocalDate.of(2012, 8, 24));
        requestDTO.setPassword("saichandu@090");
        requestDTO.setEmail("test@gmail.com");
        requestDTO.setRole("ADMIN");

        HttpEntity<Object> httpEntity=new HttpEntity<>(requestDTO);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/register", HttpMethod.POST, httpEntity,
                new ParameterizedTypeReference<ResponseStructure<RegisterResponse>>() {}));
        assertEquals(HttpStatus.BAD_REQUEST,exception.getStatusCode());
    }


    @Test
    public void loginTestValidTest()
    {
        //Register test
        UserRegisterEntity userRegisterEntity = UserRegisterEntity.builder()
                .firstName("Test")
                .lastName("Chandu")
                .dob(LocalDate.of(2002,8,24))
                .email("test@gmail.com")
                .role("ADMIN")
                .password("saichandu@090").build();

        ResponseEntity<ResponseStructure<RegisterResponse>> registerResponse = restTemplate.exchange(baseUrl + "/register", HttpMethod.POST, new HttpEntity<>(userRegisterEntity), new ParameterizedTypeReference<ResponseStructure<RegisterResponse>>(){});

        assertEquals(HttpStatus.CREATED,registerResponse.getStatusCode());
        assertEquals(HttpStatus.CREATED.value(),registerResponse.getBody().getStatus());
        assertEquals(userRegisterEntity.getEmail(),registerResponse.getBody().getData().getEmail());
        assertEquals(userRegisterEntity.getRole(),registerResponse.getBody().getData().getRole());
        assertEquals(1, userH2Repository.findAll().size());


        //Login test
        UserLoginEntity userLoginEntity = UserLoginEntity.builder()
                .email("test@gmail.com")
                .password("saichandu@090").build();

        ResponseEntity<ResponseStructure<LoginResponse>> loginResponse = restTemplate.exchange(baseUrl + "/login", HttpMethod.POST, new HttpEntity<>(userLoginEntity), new ParameterizedTypeReference<ResponseStructure<LoginResponse>>(){});

        assertEquals(HttpStatus.OK,loginResponse.getStatusCode());
        assertEquals(HttpStatus.OK.value(),loginResponse.getBody().getStatus());
        assertEquals("test@gmail.com",loginResponse.getBody().getData().getEmail());
        assertEquals("ADMIN",loginResponse.getBody().getData().getRole());
    }

    @Test
    void loginTestWithInvalidUserEmail()
    {
        //Register test
        UserRegisterEntity userRegisterEntity = UserRegisterEntity.builder()
                .firstName("Test")
                .lastName("Chandu")
                .dob(LocalDate.of(2002,8,24))
                .email("test@gmail.com")
                .role("ADMIN")
                .password("saichandu@090").build();

        ResponseEntity<ResponseStructure<RegisterResponse>> registerResponse = restTemplate.exchange(baseUrl + "/register", HttpMethod.POST, new HttpEntity<>(userRegisterEntity), new ParameterizedTypeReference<ResponseStructure<RegisterResponse>>(){});

        assertEquals(HttpStatus.CREATED,registerResponse.getStatusCode());
        assertEquals(HttpStatus.CREATED.value(),registerResponse.getBody().getStatus());
        assertEquals(userRegisterEntity.getEmail(),registerResponse.getBody().getData().getEmail());
        assertEquals(userRegisterEntity.getRole(),registerResponse.getBody().getData().getRole());
        assertEquals(1, userH2Repository.findAll().size());


        //Login test
        UserLoginEntity userLoginEntity = UserLoginEntity.builder()
                .email("tes@gmail.com")
                .password("saichandu@090").build();

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()-> restTemplate.exchange(baseUrl + "/login", HttpMethod.POST, new HttpEntity<>(userLoginEntity), new ParameterizedTypeReference<ResponseStructure<LoginResponse>>() {
        }));
        assertEquals(HttpStatus.NOT_FOUND,exception.getStatusCode());
    }


    @Test
    void loginTestWithInvalidUserPassword()
    {
        //Register test
        UserRegisterEntity userRegisterEntity = UserRegisterEntity.builder()
                .firstName("Test")
                .lastName("Chandu")
                .dob(LocalDate.of(2002,8,24))
                .email("test@gmail.com")
                .role("ADMIN")
                .password("saichandu@090").build();

        ResponseEntity<ResponseStructure<RegisterResponse>> registerResponse = restTemplate.exchange(baseUrl + "/register", HttpMethod.POST, new HttpEntity<>(userRegisterEntity), new ParameterizedTypeReference<ResponseStructure<RegisterResponse>>(){});

        assertEquals(HttpStatus.CREATED,registerResponse.getStatusCode());
        assertEquals(HttpStatus.CREATED.value(),registerResponse.getBody().getStatus());
        assertEquals(userRegisterEntity.getEmail(),registerResponse.getBody().getData().getEmail());
        assertEquals(userRegisterEntity.getRole(),registerResponse.getBody().getData().getRole());
        assertEquals(1, userH2Repository.findAll().size());


        //Login test
        UserLoginEntity userLoginEntity = UserLoginEntity.builder()
                .email("test@gmail.com")
                .password("sai@090").build();

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/login", HttpMethod.POST, new HttpEntity<>(userLoginEntity), new ParameterizedTypeReference<ResponseStructure<LoginResponse>>() {
        }));
        assertEquals(HttpStatus.UNAUTHORIZED,exception.getStatusCode());
    }


    @Test
    void isUserExistsValidTestIfUserExists()
    {
        User user= User.builder()
                .email("chandu@gmail.com").build();
        userH2Repository.save(user);

        HttpHeaders httpHeaders=new HttpHeaders();
        HttpEntity<Object> httpEntity=new HttpEntity<>(httpHeaders);

        ResponseEntity<ResponseStructure<Boolean>> response = restTemplate.exchange(baseUrl + "/isUserExists/chandu@gmail.com", HttpMethod.GET, httpEntity, new ParameterizedTypeReference<ResponseStructure<Boolean>>(){});

        assertEquals(HttpStatus.OK,response.getStatusCode(),"If user exist in database");
        assertTrue(response.getBody().getData());
        assertEquals("User exists",response.getBody().getMessage());
    }

    @Test
    void isUserExistsIfUserNotExists()
    {
        HttpHeaders httpHeaders=new HttpHeaders();
        HttpEntity<Object> httpEntity=new HttpEntity<>(httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/isUserExists/chandu@gmail.com", HttpMethod.GET, httpEntity, new ParameterizedTypeReference<ResponseStructure<Boolean>>() {}));
        assertEquals(HttpStatus.NOT_FOUND,exception.getStatusCode(),"If user does not exist in database");
    }


    @Test
    void forgetPasswordValidTestIfUserExists()
    {
        loginTestValidTest();
        HttpHeaders httpHeaders=new HttpHeaders();
        HttpEntity<Object> httpEntity=new HttpEntity<>(httpHeaders);

        ResponseEntity<ResponseStructure<Boolean>> response = restTemplate.exchange(baseUrl + "/forgetPassword/test@gmail.com?newPassword=testing@090", HttpMethod.PUT, httpEntity, new ParameterizedTypeReference<ResponseStructure<Boolean>>(){});

        assertEquals(HttpStatus.OK,response.getStatusCode(),"200 status shows that user password has been updated");
        assertTrue(response.getBody().getData());
        assertEquals("test@gmail.com password updated successfully",response.getBody().getMessage());

        User user=userH2Repository.findByEmail("test@gmail.com").get();
        assertEquals(LocalDate.now(),user.getUpdatedDate());


        //Trying to login with old password
        UserLoginEntity fail= UserLoginEntity.builder()
                .email("test@gmail.com")
                .password("saichandu@090").build();

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()->restTemplate.exchange(baseUrl + "/login", HttpMethod.POST, new HttpEntity<>(fail), new ParameterizedTypeReference<ResponseStructure<LoginResponse>>() {
        }));
        assertEquals(HttpStatus.UNAUTHORIZED,exception.getStatusCode(),"login should fail as we have updated the password for the user");


        //Trying to login after changing the password with updated password
        UserLoginEntity userLoginEntity = UserLoginEntity.builder()
                .email("test@gmail.com")
                .password("testing@090").build();

        ResponseEntity<ResponseStructure<LoginResponse>> loginResponse = restTemplate.exchange(baseUrl + "/login", HttpMethod.POST, new HttpEntity<>(userLoginEntity), new ParameterizedTypeReference<ResponseStructure<LoginResponse>>(){});

        assertEquals(HttpStatus.OK,loginResponse.getStatusCode(),"login should succeed as user have provided the updated password");
        assertEquals(HttpStatus.OK.value(),loginResponse.getBody().getStatus());
        assertEquals("test@gmail.com",loginResponse.getBody().getData().getEmail());
        assertEquals("ADMIN",loginResponse.getBody().getData().getRole());
    }


    @Test
    void forgetPasswordIfUserNotExists()
    {
        HttpHeaders httpHeaders=new HttpHeaders();
        HttpEntity<Object> httpEntity=new HttpEntity<>(httpHeaders);

        HttpClientErrorException exception=assertThrows(HttpClientErrorException.class,()-> restTemplate.exchange(baseUrl + "/forgetPassword/test@gmail.com?newPassword=testing@090", HttpMethod.PUT, httpEntity, new ParameterizedTypeReference<ResponseStructure<Boolean>>(){}));
        assertEquals(HttpStatus.NOT_FOUND,exception.getStatusCode());
        String responseBody = exception.getResponseBodyAsString();
        assertTrue(responseBody.contains("\"message\":\"User not found with email test@gmail.com\""));
    }
}
