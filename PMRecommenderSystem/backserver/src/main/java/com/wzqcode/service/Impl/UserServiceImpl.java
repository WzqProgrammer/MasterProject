package com.wzqcode.service.Impl;

import com.wzqcode.dao.UserDAO;
import com.wzqcode.entity.UserEntity;
import com.wzqcode.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author 14188
 * @date 2021/2/3 15 :33
 * @description
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDAO userDAO;

    @Override
    public UserEntity selectEntityByName(String name) {
        return userDAO.selectEntityByName(name);
    }

    @Override
    public void saveEntity(UserEntity userEntity) {
        userDAO.saveEntity(userEntity);
    }

    @Override
    public void updateEntity(UserEntity userEntity) {
        userDAO.updateEntity(userEntity);
    }

    @Override
    public void deleteEntity(Long id) {
        userDAO.deleteEntity(id);
    }
}
