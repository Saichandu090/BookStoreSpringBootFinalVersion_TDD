package com.example.bookstore.repository;

import com.example.bookstore.entity.WishList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WishListRepository extends JpaRepository<WishList,Long>
{
    Optional<WishList> findByWishListIdAndUserId(Long wishListId, Long userId);
}
