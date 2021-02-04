package com.wzqcode.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

/**
 * @author 14188
 * @date 2021/2/3 15 :05
 * @description 为每个自增的集合记录自增的序列
 */
@Data
@Document(collection = "sequenceIds")
public class SequenceId {
    @Id
    private String Id;    //主键

    @Field("collName")
    private String collName;    //集合名称

    @Field("seqId")
    private Long seqId;     //序列值
}
