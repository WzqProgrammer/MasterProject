package com.wzqcode.controller;

import ai.djl.MalformedModelException;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.translate.TranslateException;
import com.wzqcode.entity.ImageTagEntity;
import com.wzqcode.entity.TagSongEntity;
import com.wzqcode.service.ImageTagService;
import com.wzqcode.utils.LabelsInstance;
import com.wzqcode.utils.NetModelInstance;
import com.wzqcode.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author 14188
 * @date 2021/2/8 09 :24
 * @description
 */
@RestController
@RequestMapping("/songRecommend")
public class SongRecommendController {

    @Autowired
    private ImageTagService imageTagService;

    @PostMapping("/imageClassification")
    public HashMap<String, List<String>> imageClassification(@RequestParam(name = "image_data", required = false) MultipartFile file)
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
        List<String> resultTags = LabelsInstance.getLabelsInstance().getLabelResult(result);
        List<String> resultSongIds = LabelsInstance.getLabelsInstance().recommendSongs();
        List<String> resultSelectedTags = LabelsInstance.getLabelsInstance().getSelectedTags();
        HashMap<String, List<String>> data = new HashMap<>();
        data.put("tags", resultTags);
        data.put("songs", resultSongIds);
        data.put("selectedTags",resultSelectedTags);
        return data;
    }

    @PostMapping("/imageTagSave")
    public R imageTagSave(@RequestBody ImageTagEntity imageTagEntity){
        imageTagEntity.setImagePath(NetModelInstance.getInstance().GetImagePath());
        imageTagService.saveImageTagData(imageTagEntity);
        return R.ok();
    }
}
