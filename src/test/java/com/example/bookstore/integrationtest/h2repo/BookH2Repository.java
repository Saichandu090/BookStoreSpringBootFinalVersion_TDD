package com.example.bookstore.integrationtest.h2repo;

import com.example.bookstore.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookH2Repository extends JpaRepository<Book,Long>
{

}
