package com.example.demo.repository;

import com.example.demo.entity.Book;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book,Long>
{
    Optional<Book> findByBookName(String bookName);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Book b WHERE b.bookId = :bookId")
    Optional<Book> findByIdForUpdate(@Param("bookId") Long bookId);

    List<Book> findByBookNameContainingOrBookAuthorContainingOrBookDescriptionContaining(String bookName, String bookAuthor, String bookDescription);
}
