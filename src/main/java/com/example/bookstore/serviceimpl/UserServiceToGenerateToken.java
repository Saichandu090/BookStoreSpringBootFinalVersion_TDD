package com.example.bookstore.serviceimpl;

import com.example.bookstore.exception.BadCredentialsException;
import com.example.bookstore.mapper.UserMapper;
import com.example.bookstore.requestdto.UserLoginEntity;
import com.example.bookstore.responsedto.LoginResponse;
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
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserServiceToGenerateToken
{
    private JWTService jwtService;
    private ApplicationContext context;
    private AuthenticationManager authenticationManager;
    private final UserMapper userMapper=new UserMapper();

    public ResponseEntity<ResponseStructure<LoginResponse>> generateToken(UserLoginEntity loginDTO)
    {
        Authentication authentication= authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getEmail(),loginDTO.getPassword()));
        if(authentication.isAuthenticated())
            return sendToken(loginDTO);
        else
            throw new BadCredentialsException("Bad Credentials");
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
