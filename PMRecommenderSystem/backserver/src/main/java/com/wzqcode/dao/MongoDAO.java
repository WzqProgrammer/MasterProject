package com.wzqcode.dao;

import com.wzqcode.pojo.Book;

import java.util.List;

public interface MongoDAO {
    String addBook(Book book);

    //返回删除的个数
    long deleteBookById(int id);

    //返回更新的个数
    long updateBook(Book book);

    Book queryBookById(int id);

    //查询全部的Book，返回List
    List<Book> queryAllBook();
}
