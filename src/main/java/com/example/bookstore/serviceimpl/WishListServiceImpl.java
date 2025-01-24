package com.example.bookstore.serviceimpl;

import com.example.bookstore.entity.Book;
import com.example.bookstore.entity.User;
import com.example.bookstore.entity.WishList;
import com.example.bookstore.exception.BookNotFoundException;
import com.example.bookstore.exception.UserNotFoundException;
import com.example.bookstore.mapper.WishListMapper;
import com.example.bookstore.repository.BookRepository;
import com.example.bookstore.repository.UserRepository;
import com.example.bookstore.repository.WishListRepository;
import com.example.bookstore.requestdto.WishListRequest;
import com.example.bookstore.responsedto.WishListResponse;
import com.example.bookstore.service.WishListService;
import com.example.bookstore.util.ResponseStructure;
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
    public ResponseEntity<ResponseStructure<WishListResponse>> addToWishList(String email, WishListRequest wishListRequest)
    {
        User user =getUser(email);
        Book book =getBook(wishListRequest.getBookId());
        if(isInWishList(user,book.getBookId()))
            return removeBookFromWishList(user,book);
        else
            return addBookToWishList(user,book);
    }

    @Override
    public ResponseEntity<ResponseStructure<List<WishListResponse>>> getWishList(String email)
    {
        User user=getUser(email);
        List<WishList> userWishList=user.getWishList();
        if(userWishList.isEmpty())
            return wishListMapper.mapToNoContentInWishList();
        return wishListMapper.mapToSuccessGetWishList(userWishList);
    }


    @Override
    public ResponseEntity<ResponseStructure<Boolean>> isInWishList(String username, Long bookId)
    {
        User user=getUser(username);
        List<WishList> userWishList=user.getWishList();
        Optional<WishList> wishList=userWishList.stream().filter(wishList1 -> wishList1.getBookId().equals(bookId)).findFirst();
        if(wishList.isEmpty())
            return wishListMapper.mapToNotInWishList();
        return wishListMapper.mapToIsInWishList();
    }


    //Helper Methods
    public ResponseEntity<ResponseStructure<WishListResponse>> addBookToWishList(User user, Book book)
    {
        if(user.getWishList()==null)
            user.setWishList(new ArrayList<>());
        WishList wishList=wishListMapper.mapToWishList(book.getBookId(), user.getUserId());
        user.getWishList().add(wishList);
        WishList saved=wishListRepository.save(wishList);
        return wishListMapper.mapWishListSuccessResponseCREATED(saved,book);
    }


    public ResponseEntity<ResponseStructure<WishListResponse>> removeBookFromWishList(User user, Book book)
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
                return true;
        }
        return false;
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
