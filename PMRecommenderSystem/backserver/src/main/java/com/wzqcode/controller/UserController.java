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

    @PostMapping("/imageClassification")
    public List<String> imageClassification(@RequestParam(name = "image_data", required = false) MultipartFile file)
            throws IOException, TranslateException, ModelNotFoundException, MalformedModelException {
        if(file.isEmpty()){
            return null;
        }
        Image img = ImageFactory.getInstance().fromInputStream(file.getInputStream());
        System.out.println(img.getHeight());
        String result = NetModelInstance.getInstance().getClassification(img);

        //使用当前时间戳为文件命名
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss");//设置日期格式
        String currentTime = df.format(new Date().getTime());// new Date()为获取当前系统时间，也可使用当前时间戳
        String imageName = currentTime + ".jpg";
        //保存图片文件
        NetModelInstance.getInstance().saveImage(result, imageName,file);
        //将随机抽取的标签元素返回
        return LabelsInstance.getLabelsInstance().getLabelResult(result);
    }
}
