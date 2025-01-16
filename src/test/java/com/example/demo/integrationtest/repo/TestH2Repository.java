package com.example.demo.integrationtest.repo;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestH2Repository extends JpaRepository<User,Long>
{

}
