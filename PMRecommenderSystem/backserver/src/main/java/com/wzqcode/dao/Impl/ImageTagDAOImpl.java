package com.wzqcode.dao.Impl;

import com.wzqcode.dao.ImageTagDAO;
import com.wzqcode.entity.ImageTagEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

/**
 * @author 14188
 * @date 2021/2/10 10 :40
 * @description
 */
@Repository
public class ImageTagDAOImpl implements ImageTagDAO {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public void saveImageTagData(ImageTagEntity imageTagEntity) {
        mongoTemplate.save(imageTagEntity);
    }
}
