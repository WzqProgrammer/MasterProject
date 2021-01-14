package com.wzqcode.dao;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.wzqcode.pojo.Book;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Repository
public class MongoDAOImpl implements MongoDAO{

    //集合名称
    private  final String COLLECTION_NAME = "books";

    @Resource
    private MongoTemplate mongoTemplate;

    @Override
    public String addBook(Book book) {
        // 插入一条用户数据，如果文档信息已经存在就抛出异常
        Book book1 = mongoTemplate.insert(book, COLLECTION_NAME);
        if(book1 == null)
            return "failed";
        else
            return "success";
    }

    @Override
    public long deleteBookById(int id) {
        //创建条件对象
        Criteria criteria = Criteria.where("_id").is(id);
        //创建查询对象
        Query query = new Query(criteria);
        // 执行删除查找到的匹配的全部文档信息
        DeleteResult result = mongoTemplate.remove(query, COLLECTION_NAME);
        // 输出结果信息
        String resultInfo = "成功删除 " + result.getDeletedCount() + " 条文档信息";
        log.info(resultInfo);
        return result.getDeletedCount();
    }

    //更新集合中【匹配】查询到的第一条文档数据，如果没有找到就【创建并插入一个新文档】
    @Override
    public long updateBook(Book book) {
        //创建条件对象
        Criteria criteria = Criteria.where("_id").is(book.getBookID());
        //创建查询对象
        Query query = new Query(criteria);
        //创建更新对象，并设置更新的内容
        Update update = new Update()
                .set("bookName",book.getBookName())
                .set("bookCounts", book.getBookCounts())
                .set("detail", book.getDetail());
        // 执行更新，如果没有找到匹配查询的文档，则创建并插入一个新文档
        UpdateResult result = mongoTemplate.upsert(query, update, Book.class, COLLECTION_NAME);
        // 输出结果信息
        String resultInfo = "匹配到" + result.getMatchedCount() + "条数据,对第一条数据进行了更改";
        log.info("更新结果：{}", resultInfo);
        return result.getModifiedCount();
    }

    @Override
    public Book queryBookById(int id) {
        // 根据文档ID查询集合中文档数据，并转换为对应 Java 对象
        Book book = mongoTemplate.findById(id, Book.class, COLLECTION_NAME);
        return book;
    }

    @Override
    public List<Book> queryAllBook() {
        List<Book> allBooks = mongoTemplate.findAll(Book.class, COLLECTION_NAME);
        //输出结果
        for(Book book: allBooks){
            log.info("书籍信息：{}", book);
        }
        return allBooks;
    }
}
