package com.wzqcode.service;

import com.wzqcode.pojo.Book;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface BookService {
    //增加一个Book
    String addBook(Book book);

    //根据id删除一个Book
    long deleteBookById(int id);

    //更新Book
    long updateBook(Book books);

    //根据id查询,返回一个Book
    Book queryBookById(int id);

    //查询全部Book,返回list集合
    List<Book> queryAllBook();
}
