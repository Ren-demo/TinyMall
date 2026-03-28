package com.mall.tinymall.entity.document;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 商品关键词文档（MongoDB）
 * 用于存储从商品名称和描述中提取的关键词
 */
@Data
@Document(collection = "goods_keywords")
public class GoodsKeyword {

    @Id
    private String id;

    /**
     * 关键词
     */
    @Indexed(unique = true)
    private String keyword;

    /**
     * 关联的商品ID列表
     */
    private List<Integer> goodsIds;

    /**
     * 出现频率
     */
    private Integer frequency;

    /**
     * 关键词来源：name-商品名称, text-商品描述, manual-手动添加
     */
    private String source;

    /**
     * 关键词分类：food-食品, electronics-电子产品, clothing-服装, office-办公, other-其他
     */
    private String category;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdated;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    public GoodsKeyword() {
        this.frequency = 1;
        this.createdAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }

    public GoodsKeyword(String keyword, List<Integer> goodsIds, String source) {
        this();
        this.keyword = keyword;
        this.goodsIds = goodsIds;
        this.source = source;
    }
}
