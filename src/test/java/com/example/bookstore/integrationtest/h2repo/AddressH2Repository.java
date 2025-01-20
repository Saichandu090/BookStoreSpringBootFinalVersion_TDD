package com.example.bookstore.integrationtest.h2repo;

import com.example.bookstore.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressH2Repository extends JpaRepository<Address,Long>
{

}
