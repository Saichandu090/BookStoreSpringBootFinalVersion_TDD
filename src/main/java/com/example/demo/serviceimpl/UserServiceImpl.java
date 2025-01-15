package com.example.demo.serviceimpl;

import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.repository.UserRepository;
import com.example.demo.requestdto.UserLoginDTO;
import com.example.demo.requestdto.UserRegisterDTO;
import com.example.demo.responsedto.LoginResponseDto;
import com.example.demo.responsedto.RegisterResponse;
import com.example.demo.service.UserService;
import com.example.demo.util.ResponseStructure;
import com.example.demo.util.Roles;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService
{
    private UserRepository userRepository;
    private JWTService jwtService;
    private ApplicationContext context;
    private AuthenticationManager authenticationManager;
    private UserMapper userMapper;
    private final BCryptPasswordEncoder encoder=new BCryptPasswordEncoder(12);

    @Override
    public ResponseEntity<ResponseStructure<RegisterResponse>> registerUser(UserRegisterDTO registerDTO)
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
    public ResponseEntity<ResponseStructure<LoginResponseDto>> login(UserLoginDTO loginDTO)
    {
        boolean isUserExists=userRepository.existsByEmail(loginDTO.getEmail());
        if(isUserExists)
        {
            Authentication authentication= authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getEmail(),loginDTO.getPassword()));
            if(authentication.isAuthenticated())
                return sendToken(loginDTO);
        }
        return userMapper.userNotExists();
    }


    public ResponseEntity<ResponseStructure<LoginResponseDto>> sendToken(UserLoginDTO loginDTO)
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
