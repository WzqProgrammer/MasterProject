package com.wzqcode.service;

import com.wzqcode.dao.MongoDAO;
import com.wzqcode.pojo.Book;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookServiceImpl implements BookService{

    @Autowired
    private MongoDAO mongoDAO;

    @Override
    public String addBook(Book book) {
        return mongoDAO.addBook(book);
    }

    @Override
    public long deleteBookById(int id) {
        return mongoDAO.deleteBookById(id);
    }

    @Override
    public long updateBook(Book book) {
        return mongoDAO.updateBook(book);
    }

    @Override
    public Book queryBookById(int id) {
        return mongoDAO.queryBookById(id);
    }

    @Override
    public List<Book> queryAllBook() {
        return mongoDAO.queryAllBook();
    }
}
