package com.example.demo.integrationtest.h2repo;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserH2Repository extends JpaRepository<User,Long>
{
    Optional<User> findByEmail(String email);
}
