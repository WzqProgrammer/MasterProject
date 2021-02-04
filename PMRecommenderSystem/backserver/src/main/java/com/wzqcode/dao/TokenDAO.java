package com.wzqcode.dao;

import com.wzqcode.entity.TokenEntity;
import com.wzqcode.entity.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

/**
 * @author 14188
 * @date 2021/2/4 10 :35
 * @description
 */
public interface TokenDAO {
    TokenEntity selectEntityByToken(String token);

    TokenEntity selectEntityByUserId(Long userId);

    void saveEntity(TokenEntity tokenEntity);

    void updateEntity(TokenEntity tokenEntity);

    void deleteEntity(Long id);
}
