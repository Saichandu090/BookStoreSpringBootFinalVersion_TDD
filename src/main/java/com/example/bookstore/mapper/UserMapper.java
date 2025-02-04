package com.example.bookstore.mapper;

import com.example.bookstore.entity.User;
import com.example.bookstore.requestdto.UserRegisterEntity;
import com.example.bookstore.responsedto.LoginResponse;
import com.example.bookstore.responsedto.RegisterResponse;
import com.example.bookstore.serviceimpl.JWTService;
import com.example.bookstore.serviceimpl.MyUserDetailsService;
import com.example.bookstore.util.ResponseStructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class UserMapper
{
    @Autowired
    JWTService jwtService;

    @Autowired
    ApplicationContext context;

    public UserDetails validateUserToken(String authHeader)
    {
        if(authHeader==null)
            return null;

        String token=null;
        String email=null;
        if(authHeader.startsWith("Bearer "))
        {
            token=authHeader.substring(7);
            email=jwtService.extractEmail(token);
        }
        UserDetails userDetails=context.getBean(MyUserDetailsService.class).loadUserByUsername(email);
        if(jwtService.validateToken(token,userDetails))
            return userDetails;
        else
            return null;
    }

    public ResponseEntity<ResponseStructure<RegisterResponse>> userAlreadyExists()
    {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ResponseStructure<RegisterResponse>()
                .setMessage("User Already Registered")
                .setStatus(HttpStatus.CONFLICT.value())
                .setData(null));
    }

    public User convertFromRegisterDTO(UserRegisterEntity registerDTO)
    {
        return User.builder()
                .email(registerDTO.getEmail())
                .dob(registerDTO.getDob())
                .registeredDate(LocalDate.now())
                .updatedDate(null)
                .firstName(registerDTO.getFirstName())
                .lastName(registerDTO.getLastName())
                .role(registerDTO.getRole())
                .password(registerDTO.getPassword()).build();
    }

    public ResponseEntity<ResponseStructure<RegisterResponse>> convertUser(User savedUser)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(new ResponseStructure<RegisterResponse>()
                .setMessage("User "+savedUser.getEmail()+" has registered successfully")
                .setStatus(HttpStatus.CREATED.value())
                .setData(RegisterResponse.builder().userId(savedUser.getUserId()).email(savedUser.getEmail()).role(savedUser.getRole()).build()));
    }

    public ResponseEntity<ResponseStructure<LoginResponse>> loginSuccess(String token, String email, String role)
    {
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<LoginResponse>()
                .setStatus(HttpStatus.OK.value())
                .setMessage(token)
                .setData(LoginResponse.builder().role(role).email(email).build()));
    }

    public ResponseEntity<ResponseStructure<Boolean>> mapToFailureUserNotExist()
    {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseStructure<Boolean>()
                .setData(false)
                .setMessage("User not exists")
                .setStatus(HttpStatus.NOT_FOUND.value()));
    }

    public ResponseEntity<ResponseStructure<Boolean>> mapToSuccessUserExists()
    {
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<Boolean>()
                .setStatus(HttpStatus.OK.value())
                .setMessage("User exists")
                .setData(true));
    }

    public ResponseEntity<ResponseStructure<Boolean>> mapToSuccessPasswordUpdated(User updatedUser)
    {
        return ResponseEntity.status(HttpStatus.OK).body(new ResponseStructure<Boolean>()
                .setData(true)
                .setStatus(HttpStatus.OK.value())
                .setMessage(updatedUser.getEmail()+" password updated successfully"));
    }

    public ResponseEntity<ResponseStructure<LoginResponse>> mapToBadCredentials()
    {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ResponseStructure<LoginResponse>()
                .setMessage("Bad Credentials")
                .setData(null)
                .setStatus(HttpStatus.UNAUTHORIZED.value()));
    }
}
