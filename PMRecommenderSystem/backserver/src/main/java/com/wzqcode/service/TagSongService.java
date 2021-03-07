package com.wzqcode.service;

import com.wzqcode.entity.TagSongEntity;

/**
 * @author 14188
 * @date 2021/2/8 09 :19
 * @description
 */
public interface TagSongService {

    TagSongEntity findSongIdsByTag(String tagName);
}
