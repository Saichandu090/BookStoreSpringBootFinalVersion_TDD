package com.example.demo.serviceimpl;

import com.example.demo.requestdto.BookRequestDTO;
import com.example.demo.repository.BookRepository;
import com.example.demo.responsedto.BookResponseDTO;
import com.example.demo.service.BookService;
import com.example.demo.util.ResponseStructure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class BookServiceImpl implements BookService
{
    @Autowired
    private BookRepository bookRepository;

    @Override
    public ResponseEntity<ResponseStructure<BookResponseDTO>> addBook(BookRequestDTO bookRequestDTO)
    {
        return null;
    }
}
