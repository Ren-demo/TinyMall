package com.mall.tinymall.entity.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @date 2026/3/17 15:59
 * @description TODO:
 */
@AllArgsConstructor
@Data
@Schema(description = "分页查询商品传递信息")
public class ListGoodsDto {
    @Schema(description = "页码")
    private Integer pageNum=1;
    @Schema(description = "每页大小")
    private Integer pageSize=10;
}
