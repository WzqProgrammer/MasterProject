package com.wzqcode.dao.Impl;

import com.wzqcode.dao.TokenDAO;
import com.wzqcode.entity.TokenEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

/**
 * @author 14188
 * @date 2021/2/4 10 :45
 * @description
 */
@Repository
public class TokenDAOImpl implements TokenDAO {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public TokenEntity selectEntityByToken(String token) {
        Query query = new Query(Criteria.where("token").is(token));
        return mongoTemplate.findOne(query, TokenEntity.class);
    }

    @Override
    public TokenEntity selectEntityByUserId(Long userId) {
        Query query = new Query(Criteria.where("userId").is(userId));
        return mongoTemplate.findOne(query, TokenEntity.class);
    }

    @Override
    public void saveEntity(TokenEntity tokenEntity) {
        mongoTemplate.insert(tokenEntity);
    }

    @Override
    public void updateEntity(TokenEntity tokenEntity) {
        Query query = new Query(Criteria.where("_id").is(tokenEntity.getId()));
        Update update = new Update()
                .set("userId", tokenEntity.getUserId())
                .set("userName", tokenEntity.getUserName())
                .set("token", tokenEntity.getToken());
        mongoTemplate.updateFirst(query, update, tokenEntity.getClass());
    }

    @Override
    public void deleteEntity(Long id) {
        Query query = new Query(Criteria.where("_id").is(id));
        mongoTemplate.remove(query, TokenEntity.class);
    }
}
