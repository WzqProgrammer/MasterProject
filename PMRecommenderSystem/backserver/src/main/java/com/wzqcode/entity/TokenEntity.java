package com.wzqcode.entity;

import com.wzqcode.annotation.AutoIncrement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

/**
 * @author 14188
 * @date 2021/2/4 10 :11
 * @description token表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "Token")
public class TokenEntity {
    @Id
    @AutoIncrement   //注解说明该成员为自增id
    @Field("_id")
    private long id;  //注意不要设置成Long包装类

    @Field("userId")
    private Long userId;

    @Field("userName")
    private String userName;

    @Field("token")
    private String token;

    //token过期时间
    @Field("expiratedtime")
    private Date expiratedtime;

    //token增加时间
    @Field("addtime")
    private Date addtime;
}
