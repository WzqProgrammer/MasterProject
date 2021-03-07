package com.wzqcode.entity;

import com.wzqcode.annotation.AutoIncrement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.util.Date;

/**
 * @author 14188
 * @date 2021/2/6 15 :16
 * @description 用户针对音乐的评分
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "UserSongMark")
public class UserSongMarkEntity {

    @MongoId
    private ObjectId id;

    @Field("userId")
    private Long userId;

    @Field("songId")
    private Long songId;

    @Field("mark")
    private float mark;

    //评分时间戳
    @Field("markTime")
    private Long markTime;

    //图片路径
    @Field("imagePath")
    private String imagePath;
}
