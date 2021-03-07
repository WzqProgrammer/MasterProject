package com.wzqcode.dao.Impl;

import com.wzqcode.dao.TagSongDAO;
import com.wzqcode.entity.TagSongEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

/**
 * @author 14188
 * @date 2021/2/6 16 :19
 * @description
 */
@Repository
public class TagSongDAOImpl implements TagSongDAO {
    @Autowired
    private MongoTemplate mongoTemplate;


    @Override
    public TagSongEntity selectByTag(String tagName) {
        Query query = new Query(org.springframework.data.mongodb.core.query.Criteria.where("genres").is(tagName));
        return mongoTemplate.findOne(query, TagSongEntity.class);
    }
}
