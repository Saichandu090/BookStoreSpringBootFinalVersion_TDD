package com.example.demo.integrationtest.h2repo;

import com.example.demo.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressH2Repository extends JpaRepository<Address,Long>
{

}
