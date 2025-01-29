package com.example.bookstore.serviceimpl;

import com.example.bookstore.entity.User;
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
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService
{
    private UserRepository userRepository;
    private final UserMapper userMapper=new UserMapper();
    private PasswordEncoder encoder;
    private UserServiceToGenerateToken userServiceToGenerateToken;

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
            return userServiceToGenerateToken.generateToken(loginDTO);
        else
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
        User realUser=getUser(newPasswordRequest.getEmail());
        realUser.setPassword(encoder.encode(newPasswordRequest.getPassword()));
        realUser.setUpdatedDate(LocalDate.now());
        User updatedUser=userRepository.save(realUser);
        return userMapper.mapToSuccessPasswordUpdated(updatedUser);
    }

    private User getUser(String email)
    {
        Optional<User> user=userRepository.findByEmail(email);
        if(user.isEmpty())
            throw new UserNotFoundException("User not found with email "+email);
        return user.get();
    }
}
