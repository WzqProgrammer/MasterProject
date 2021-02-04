package com.wzqcode.service;

import com.wzqcode.entity.UserEntity;
import org.springframework.stereotype.Service;

/**
 * @author 14188
 * @date 2021/2/3 15 :30
 * @description
 */

public interface UserService {
    UserEntity selectEntityByName(String name);

    void saveEntity(UserEntity userEntity);

    void updateEntity(UserEntity userEntity);

    void deleteEntity(Long id);
}
