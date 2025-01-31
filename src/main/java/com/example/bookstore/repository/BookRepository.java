package com.example.bookstore.repository;

import com.example.bookstore.entity.Book;
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

    boolean existsByBookName(String bookName);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Book b WHERE b.bookId = :bookId")
    Optional<Book> findByIdForUpdate(@Param("bookId") Long bookId);

    @Query("SELECT b FROM Book b WHERE b.bookName LIKE %:keyword% OR b.bookAuthor LIKE %:keyword% OR b.bookDescription LIKE %:keyword%")
    List<Book> searchBooksByKeyword(@Param("keyword") String keyword);
}
