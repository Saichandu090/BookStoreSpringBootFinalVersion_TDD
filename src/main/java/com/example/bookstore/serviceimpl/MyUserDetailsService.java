package com.example.bookstore.serviceimpl;

import com.example.bookstore.entity.User;
import com.example.bookstore.entity.UserPrinciple;
import com.example.bookstore.exception.UserNotFoundException;
import com.example.bookstore.repository.UserRepository;
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
