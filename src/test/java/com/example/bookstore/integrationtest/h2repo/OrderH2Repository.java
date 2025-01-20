package com.example.bookstore.integrationtest.h2repo;

import com.example.bookstore.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderH2Repository extends JpaRepository<Order,Long>
{

}
