package com.wzqcode.entity;

import com.wzqcode.annotation.AutoIncrement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

/**
 * @author 14188
 * @date 2021/2/10 10 :35
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "ImageTags")
public class ImageTagEntity {
    @Id
    @AutoIncrement   //注解说明该成员为自增id
    @Field("_id")
    private long id;  //注意不要设置成Long包装类

    @Field("imagePath")
    private String imagePath;

    @Field("tags")
    private List<String> tagList;
}
