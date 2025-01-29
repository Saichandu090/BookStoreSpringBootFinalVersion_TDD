package com.example.bookstore.service;

import com.example.bookstore.entity.User;
import com.example.bookstore.exception.UserNotFoundException;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.requestdto.NewPasswordRequest;
import com.example.bookstore.requestdto.UserLoginEntity;
import com.example.bookstore.requestdto.UserRegisterEntity;
import com.example.bookstore.responsedto.LoginResponse;
import com.example.bookstore.responsedto.RegisterResponse;
import com.example.bookstore.serviceimpl.UserServiceImpl;
import com.example.bookstore.serviceimpl.UserServiceToGenerateToken;
import com.example.bookstore.util.ResponseStructure;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest
{
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserServiceToGenerateToken userServiceToGenerateToken;

    @Mock
    private PasswordEncoder encoder;

    private User user;
    private UserRegisterEntity registerDTO;
    private UserLoginEntity userLoginEntity;

    @BeforeEach
    public void init()
    {
        registerDTO= UserRegisterEntity.builder()
                .firstName("Sai")
                .lastName("Chandu")
                .dob(LocalDate.of(2002,8,24))
                .email("test@gmail.com")
                .password("testing")
                .role("ADMIN").build();

        user=User.builder()
                .userId(1L)
                .firstName(registerDTO.getFirstName())
                .lastName(registerDTO.getLastName())
                .dob(registerDTO.getDob())
                .email(registerDTO.getEmail())
                .password(registerDTO.getPassword())
                .role(registerDTO.getRole()).build();

        userLoginEntity = UserLoginEntity.builder()
                .email("test@gmail.com")
                .password("testing").build();
    }


    @Test
    void registerUserTestMustPassWithValidBody()
    {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(encoder.encode(anyString())).thenReturn("new password");

        ResponseEntity<ResponseStructure<RegisterResponse>> response=userService.registerUser(registerDTO);

        assertEquals(HttpStatus.CREATED,response.getStatusCode());
        assertEquals(HttpStatus.CREATED.value(),response.getBody().getStatus());
        assertEquals(user.getEmail(),response.getBody().getData().getEmail());
        assertEquals(user.getRole(),response.getBody().getData().getRole());
    }

    @Test
    void registerUserTestIfUserAlreadyExists()
    {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        ResponseEntity<ResponseStructure<RegisterResponse>> response=userService.registerUser(registerDTO);

        assertEquals(HttpStatus.CONFLICT,response.getStatusCode());
        assertEquals(HttpStatus.CONFLICT.value(),response.getBody().getStatus());
    }


    @Test
    void loginUserValidScene()
    {
        when(userRepository.existsByEmail(userLoginEntity.getEmail())).thenReturn(true);
//        UsernamePasswordAuthenticationToken authenticationToken=new UsernamePasswordAuthenticationToken(userLoginEntity.getEmail(), userLoginEntity.getPassword());
//        Authentication authenticationResult= Mockito.mock(Authentication.class);
//        when(authenticationResult.isAuthenticated()).thenReturn(true);
//        when(authenticationManager.authenticate(authenticationToken)).thenReturn(authenticationResult);
//        when(jwtService.generateToken(userLoginEntity.getEmail())).thenReturn("jwt-token");
        //when(context.getBean(MyUserDetailsService.class)).thenReturn(myUserDetailsService);
        //when(myUserDetailsService.loadUserByUsername(userLoginEntity.getEmail())).thenReturn(userDetails);
        ResponseEntity<ResponseStructure<LoginResponse>> structureResponseEntity=new ResponseEntity<>(new ResponseStructure<LoginResponse>().setStatus(200).setMessage("jwt-token").setData(new LoginResponse("test@gmail.com","ADMIN")),HttpStatus.OK);
        when(userServiceToGenerateToken.generateToken(any(UserLoginEntity.class))).thenReturn(structureResponseEntity);

        ResponseEntity<ResponseStructure<LoginResponse>> response=userService.login(userLoginEntity);
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(HttpStatus.OK.value(),response.getBody().getStatus());
        assertEquals("jwt-token",response.getBody().getMessage());
        assertEquals(userLoginEntity.getEmail(),response.getBody().getData().getEmail());
    }


    @Test
    void loginUserIfUserNotRegistered()
    {
        when(userRepository.existsByEmail(userLoginEntity.getEmail())).thenReturn(false);
        assertThrows(UserNotFoundException.class,()->userService.login(userLoginEntity));
        verify(userRepository,times(1)).existsByEmail(anyString());
    }


    @Test
    void isUserExistsIfUserExist()
    {
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        ResponseEntity<ResponseStructure<Boolean>> response=userService.isUserExists("marrisaichandu143@gmail.com");
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals("User exists",response.getBody().getMessage());
        assertTrue(response.getBody().getData());
    }


    @Test
    void isUserExistsIfUserNotExist()
    {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);

        ResponseEntity<ResponseStructure<Boolean>> response=userService.isUserExists("marrisaichandu143@gmail.com");
        assertEquals(HttpStatus.NOT_FOUND,response.getStatusCode());
        assertEquals("User not exists",response.getBody().getMessage());
        assertFalse(response.getBody().getData());
    }

    @Test
    void forgetPasswordIfUserExist()
    {
        NewPasswordRequest request=NewPasswordRequest.builder().email("something@gmail.com").password("new password").build();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        ResponseEntity<ResponseStructure<Boolean>> response=userService.forgetPassword(request);
        assertEquals(HttpStatus.OK,response.getStatusCode());
        assertEquals(user.getEmail()+" password updated successfully",response.getBody().getMessage());
        assertTrue(response.getBody().getData());
    }

    @Test
    void forgetPasswordIfUserNotExists()
    {
        NewPasswordRequest request=NewPasswordRequest.builder().email("something@gmail.com").password("new password").build();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,()->userService.forgetPassword(request));

        verify(userRepository,times(1)).findByEmail(anyString());
    }
}