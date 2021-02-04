package com.wzqcode.entity;

import com.wzqcode.annotation.AutoIncrement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author 14188
 * @date 2021/2/3 10 :46
 * @description
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "userTest")
public class UserEntity {

    @Id
    @AutoIncrement   //注解说明该成员为自增id
    @Field("_id")
    private long id;  //注意不要设置成Long包装类

    @Field("userName")
    private String userName;

    @Field("password")
    private String password;
}
