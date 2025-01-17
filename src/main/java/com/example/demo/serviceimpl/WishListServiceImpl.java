package com.example.demo.serviceimpl;

import com.example.demo.entity.Book;
import com.example.demo.entity.User;
import com.example.demo.entity.WishList;
import com.example.demo.exception.BookNotFoundException;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.mapper.WishListMapper;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.WishListRepository;
import com.example.demo.requestdto.WishListRequestDto;
import com.example.demo.responsedto.WishListResponseDto;
import com.example.demo.service.WishListService;
import com.example.demo.util.ResponseStructure;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class WishListServiceImpl implements WishListService
{
    private UserRepository userRepository;
    private BookRepository bookRepository;
    private WishListRepository wishListRepository;
    private final WishListMapper wishListMapper=new WishListMapper();


    @Override
    public ResponseEntity<ResponseStructure<WishListResponseDto>> addToWishList(String email, WishListRequestDto wishListRequestDto)
    {
        User user =getUser(email);
        Book book =getBook(wishListRequestDto.getBookId());
        if(isInWishList(user,book.getBookId()))
            return addBookToWishList(user,book);
        else
            return removeBookFromWishList(user,book);
    }


    //Helper Methods
    public ResponseEntity<ResponseStructure<WishListResponseDto>> addBookToWishList(User user, Book book)
    {
        if(user.getWishList()==null)
            user.setWishList(new ArrayList<>());
        WishList wishList=wishListMapper.mapToWishList(book.getBookId(), user.getUserId());
        user.getWishList().add(wishList);
        WishList saved=wishListRepository.save(wishList);
        return wishListMapper.mapWishListSuccessResponseCREATED(saved);
    }


    public ResponseEntity<ResponseStructure<WishListResponseDto>> removeBookFromWishList(User user, Book book)
    {
        List<WishList> wishLists=user.getWishList();
        wishLists.removeIf(wishList -> wishList.getBookId().equals(book.getBookId()));
        wishListRepository.saveAll(wishLists);
        return wishListMapper.mapToSuccessResponseOk(book.getBookName());
    }


    public boolean isInWishList(User user, Long bookId)
    {
        List<WishList> wishLists=user.getWishList();
        for(WishList wishList:wishLists)
        {
            if(wishList.getBookId().equals(bookId))
                return false;
        }
        return true;
    }


    public User getUser(String email)
    {
        Optional<User> user=userRepository.findByEmail(email);
        if(user.isEmpty())
            throw new UserNotFoundException("User with username "+email+" not found");
        return user.get();
    }

    public Book getBook(Long bookId)
    {
        Optional<Book> book=bookRepository.findById(bookId);
        if(book.isEmpty())
            throw new BookNotFoundException("Book not found with id "+bookId);
        return book.get();
    }
}
