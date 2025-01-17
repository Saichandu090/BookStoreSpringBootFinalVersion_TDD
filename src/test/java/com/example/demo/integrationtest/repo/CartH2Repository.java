package com.example.demo.integrationtest.repo;

import com.example.demo.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartH2Repository extends JpaRepository<Cart,Long>
{

}
