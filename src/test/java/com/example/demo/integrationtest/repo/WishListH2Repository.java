package com.example.demo.integrationtest.repo;

import com.example.demo.entity.WishList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishListH2Repository extends JpaRepository<WishList,Long>
{

}
