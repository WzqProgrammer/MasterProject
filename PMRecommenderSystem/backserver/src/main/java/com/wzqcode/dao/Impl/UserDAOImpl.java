package com.wzqcode.dao.Impl;

import com.wzqcode.dao.UserDAO;
import com.wzqcode.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

/**
 * @author 14188
 * @date 2021/2/3 14 :56
 * @description
 */
@Repository
class UserDAOImpl implements UserDAO {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public UserEntity selectEntityByName(String name) {
        Query query = new Query(Criteria.where("userName").is(name));
        return mongoTemplate.findOne(query, UserEntity.class);
    }

    @Override
    public void saveEntity(UserEntity userEntity) {
        mongoTemplate.insert(userEntity);
    }

    @Override
    public void updateEntity(UserEntity userEntity) {
        Query query = new Query(Criteria.where("_id").is(userEntity.getId()));
        Update update = new Update().set("userName", userEntity.getUserName()).set("password", userEntity.getPassword());
        mongoTemplate.updateFirst(query, update, userEntity.getClass());
    }

    @Override
    public void deleteEntity(Long id) {
        Query query = new Query(Criteria.where("_id").is(id));
        mongoTemplate.remove(query, UserEntity.class);
    }
}
