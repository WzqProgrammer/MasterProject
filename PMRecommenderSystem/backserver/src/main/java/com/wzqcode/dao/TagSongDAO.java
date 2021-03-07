package com.wzqcode.dao;

import com.wzqcode.entity.TagSongEntity;

/**
 * @author 14188
 * @date 2021/2/7 11 :32
 * @description
 */

public interface TagSongDAO {

    TagSongEntity selectByTag(String tagName);
}
