package com.example.demo.integrationtest.repo;

import com.example.demo.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookH2Repository extends JpaRepository<Book,Long>
{

}
