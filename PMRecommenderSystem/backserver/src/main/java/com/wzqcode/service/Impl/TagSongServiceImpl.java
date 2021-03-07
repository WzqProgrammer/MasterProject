package com.wzqcode.service.Impl;

import com.wzqcode.dao.TagSongDAO;
import com.wzqcode.entity.TagSongEntity;
import com.wzqcode.service.TagSongService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author 14188
 * @date 2021/2/8 09 :21
 * @description
 */

@Service
public class TagSongServiceImpl implements TagSongService {

    @Autowired
    private TagSongDAO tagSongDAO;


    @Override
    public TagSongEntity findSongIdsByTag(String tagName) {
        return tagSongDAO.selectByTag(tagName);
    }
}
