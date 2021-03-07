package com.wzqcode.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.HashMap;
import java.util.List;

/**
 * @author 14188
 * @date 2021/2/7 10 :42
 * @description
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "TagsTopSongs")
public class TagSongEntity {

    @Field("genres")
    private String genres;

    @Field("recs")
    private List<HashMap<String, Long>> recs;
}
