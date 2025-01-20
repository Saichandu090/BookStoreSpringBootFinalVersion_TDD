package com.example.bookstore.integrationtest.h2repo;

import com.example.bookstore.entity.WishList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishListH2Repository extends JpaRepository<WishList,Long>
{

}
