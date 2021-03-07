package com.wzqcode.controller;

import com.wzqcode.entity.TokenEntity;
import com.wzqcode.entity.UserSongMarkEntity;
import com.wzqcode.service.TokenService;
import com.wzqcode.service.UserSongMarkService;
import com.wzqcode.utils.NetModelInstance;
import com.wzqcode.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author 14188
 * @date 2021/2/6 15 :42
 * @description
 */
@RestController
@RequestMapping("/mark")
public class MarkController {

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserSongMarkService userSongMarkService;

    @PostMapping("/userSongMark")
    public R userSongMark(@RequestBody UserSongMarkEntity userSongMarkEntity, @RequestHeader("token") String token){
        //用户对推荐歌曲的评分
        //根据请求头的token获取用户信息
        TokenEntity tokenEntity = tokenService.selectEntityByToken(token);
        userSongMarkEntity.setUserId(tokenEntity.getUserId());
        userSongMarkEntity.setMarkTime(System.currentTimeMillis());
        userSongMarkEntity.setImagePath(NetModelInstance.getInstance().GetImagePath());
        userSongMarkService.saveEntity(userSongMarkEntity);
        return R.ok();
    }
}
