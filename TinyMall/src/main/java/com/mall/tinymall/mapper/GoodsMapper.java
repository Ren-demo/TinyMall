package com.mall.tinymall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mall.tinymall.entity.pojo.Goods;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

public interface GoodsMapper extends BaseMapper<Goods> {
    @Select("select * from goods where GoodsID=#{goodsId} for update")
    Goods selectGoodsById(Integer goodsId);

    @Select("select GoodsName from goods where GoodsID=#{goodsId}")
    String getGoodsName(Integer goodsId);

    @Select("select `Count` from goods where GoodsID=#{goodsId} for update")
    Integer selectGoodsCount(Integer goodsId);

    @Update("update goods set Count=#{count} where GoodsID=#{goodsId}")
    void updateGoodsCount(Integer goodsId, int count);

    @Select("select `Count` from goods where GoodsID=#{goodsId} for update")
    Integer getGoodsCount(Integer goodsId);

    @Select("select `Picture` from goods where GoodsID=#{goodsId}")
    String getGoodsPicture(Integer goodsId);

    @Update("update goods set Picture=#{url} where GoodsID=#{goodsId}")
    void saveGoodsPicture(Integer goodsId, String url);
}
