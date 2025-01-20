package com.example.bookstore.integrationtest.h2repo;

import com.example.bookstore.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartH2Repository extends JpaRepository<Cart,Long>
{

}
