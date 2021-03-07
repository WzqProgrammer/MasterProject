package com.wzqcode.service.Impl;

import com.wzqcode.dao.UserSongMarkDAO;
import com.wzqcode.entity.UserSongMarkEntity;
import com.wzqcode.service.UserSongMarkService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author 14188
 * @date 2021/2/6 16 :22
 * @description
 */
@Service
public class UserSongMarkServiceImpl implements UserSongMarkService {

    @Autowired
    private UserSongMarkDAO userSongMarkDAO;

    @Override
    public void saveEntity(UserSongMarkEntity userSongMarkEntity) {
        userSongMarkDAO.saveEntity(userSongMarkEntity);
    }
}
