package com.wzqcode.controller;

import ai.djl.MalformedModelException;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.translate.TranslateException;
import com.wzqcode.annotation.IgnoreAuth;
import com.wzqcode.entity.UserEntity;
import com.wzqcode.service.TokenService;
import com.wzqcode.service.UserService;
import com.wzqcode.utils.LabelsInstance;
import com.wzqcode.utils.NetModelInstance;
import com.wzqcode.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TokenService tokenService;

    @IgnoreAuth
    @PostMapping("/login")
    public R loginUser(@RequestBody UserEntity userEntity){
        UserEntity user = userService.selectEntityByName(userEntity.getUserName());
        if(user==null || !user.getPassword().equals(userEntity.getPassword())){
            return R.error("账号或密码错误");
        }
        String token = tokenService.generateToken(user.getId(), user.getUserName());
        return R.ok().put("token", token);
    }

    @IgnoreAuth
    @PostMapping("/register")
    public R registerUser(@RequestBody UserEntity userEntity){
        UserEntity user = userService.selectEntityByName(userEntity.getUserName());

        if(user!=null){
            return R.error("用户已存在");
        }
        userService.saveEntity(userEntity);
        return R.ok();
    }


}
