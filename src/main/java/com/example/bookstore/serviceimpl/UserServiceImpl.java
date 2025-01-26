package com.example.bookstore.serviceimpl;

import com.example.bookstore.entity.User;
import com.example.bookstore.exception.BadCredentialsException;
import com.example.bookstore.exception.UserNotFoundException;
import com.example.bookstore.mapper.UserMapper;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.requestdto.NewPasswordRequest;
import com.example.bookstore.requestdto.UserLoginEntity;
import com.example.bookstore.requestdto.UserRegisterEntity;
import com.example.bookstore.responsedto.LoginResponse;
import com.example.bookstore.responsedto.RegisterResponse;
import com.example.bookstore.service.UserService;
import com.example.bookstore.util.ResponseStructure;
import com.example.bookstore.util.Roles;
import lombok.AllArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService
{
    private UserRepository userRepository;
    private JWTService jwtService;
    private ApplicationContext context;
    private AuthenticationManager authenticationManager;
    private final UserMapper userMapper=new UserMapper();
    private PasswordEncoder encoder;

    @Override
    public ResponseEntity<ResponseStructure<RegisterResponse>> registerUser(UserRegisterEntity registerDTO)
    {
        boolean isUserExist=userRepository.existsByEmail(registerDTO.getEmail());
        if(isUserExist){
            return userMapper.userAlreadyExists();
        }
        User newUser=userMapper.convertFromRegisterDTO(registerDTO);
        newUser.setPassword(encoder.encode(newUser.getPassword()));
        User savedUser=userRepository.save(newUser);
        return userMapper.convertUser(savedUser);
    }

    @Override
    public ResponseEntity<ResponseStructure<LoginResponse>> login(UserLoginEntity loginDTO)
    {
        boolean isUserExists=userRepository.existsByEmail(loginDTO.getEmail());
        if(isUserExists)
        {
            Authentication authentication= authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getEmail(),loginDTO.getPassword()));
            if(authentication.isAuthenticated())
                return sendToken(loginDTO);
            else
                throw new BadCredentialsException("Bad Credentials");
        }else
            throw new UserNotFoundException("User not Found");
    }

    @Override
    public ResponseEntity<ResponseStructure<Boolean>> isUserExists(String email)
    {
        boolean exists=userRepository.existsByEmail(email);
        if(exists)
            return userMapper.mapToSuccessUserExists();
        else
            return userMapper.mapToFailureUserNotExist();
    }

    @Override
    public ResponseEntity<ResponseStructure<Boolean>> forgetPassword(NewPasswordRequest newPasswordRequest)
    {
        Optional<User> user=userRepository.findByEmail(newPasswordRequest.getEmail());
        if(user.isEmpty())
            throw new UserNotFoundException("User not found with email "+newPasswordRequest.getEmail());
        User realUser=user.get();
        realUser.setPassword(encoder.encode(newPasswordRequest.getPassword()));
        realUser.setUpdatedDate(LocalDate.now());
        User updatedUser=userRepository.save(realUser);
        return userMapper.mapToSuccessPasswordUpdated(updatedUser);
    }


    public ResponseEntity<ResponseStructure<LoginResponse>> sendToken(UserLoginEntity loginDTO)
    {
        String token=jwtService.generateToken(loginDTO.getEmail());
        UserDetails userDetails=context.getBean(MyUserDetailsService.class).loadUserByUsername(loginDTO.getEmail());
        String role=null;
        if(userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.USER.name())))
            role=Roles.USER.name();
        else if(userDetails.getAuthorities().contains(new SimpleGrantedAuthority(Roles.ADMIN.name())))
            role=Roles.ADMIN.name();
        return userMapper.loginSuccess(token,loginDTO.getEmail(),role);
    }
}
