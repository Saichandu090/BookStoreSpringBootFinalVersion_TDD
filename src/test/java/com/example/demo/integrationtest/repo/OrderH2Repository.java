package com.example.demo.integrationtest.repo;

import com.example.demo.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderH2Repository extends JpaRepository<Order,Long>
{

}
