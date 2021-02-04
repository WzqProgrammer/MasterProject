package com.wzqcode.dao;

import com.wzqcode.entity.UserEntity;

/**
 * @author 14188
 * @date 2021/2/3 14 :51
 * @description
 */

public interface UserDAO {
    UserEntity selectEntityByName(String name);

    void saveEntity(UserEntity userEntity);

    void updateEntity(UserEntity userEntity);

    void deleteEntity(Long id);
}
