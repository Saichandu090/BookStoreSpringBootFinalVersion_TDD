package com.example.demo.serviceimpl;

import com.example.demo.entity.User;
import com.example.demo.entity.UserPrinciple;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService
{
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException
    {
        User user=userRepository.findByEmail(username).orElseThrow(()->new UserNotFoundException("User Not Found"));
        return new UserPrinciple(user);
    }
}
