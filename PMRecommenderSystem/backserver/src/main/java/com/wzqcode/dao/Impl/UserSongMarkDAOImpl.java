package com.wzqcode.dao.Impl;

import com.wzqcode.dao.UserSongMarkDAO;
import com.wzqcode.entity.UserSongMarkEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

/**
 * @author 14188
 * @date 2021/2/6 16 :19
 * @description
 */
@Repository
public class UserSongMarkDAOImpl implements UserSongMarkDAO {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void saveEntity(UserSongMarkEntity userSongMarkEntity) {
        mongoTemplate.insert(userSongMarkEntity);
    }
}
