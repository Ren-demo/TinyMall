package com.mall.tinymall.entity.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

import java.util.List;

/**
 * @date 2026/3/18 13:58
 * @description TODO: 评论实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "TinyMall_Comment")
public class Comment {
    @Id
    @Field(targetType = FieldType.OBJECT_ID)
    private String id;
    private Integer goodsId;
    private Integer orderId;
    private Integer userId;
    private Integer star;
    private String content;
    private List<String> images;

}
