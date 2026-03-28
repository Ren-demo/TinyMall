package com.mall.tinymall.service.Impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mall.tinymall.entity.dto.AddComDto;
import com.mall.tinymall.entity.dto.UpdateComDto;
import com.mall.tinymall.entity.pojo.Comment;
import com.mall.tinymall.entity.pojo.Order;
import com.mall.tinymall.entity.pojo.Result;
import com.mall.tinymall.mapper.OrderMapper;
import com.mall.tinymall.service.CommentsService;
import com.mall.tinymall.service.OrderService;
import com.mall.tinymall.util.MinioUtil;
import com.mall.tinymall.util.MongoDbUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @date 2026/3/18 16:59
 * @description TODO: 商品评论业务处理
 */
@Service
@Slf4j
public class CommentsServiceImpl implements CommentsService {

    @Autowired
    private MinioUtil minioUtil;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private MongoDbUtils mongoDbUtils;

    @Override
    public String saveImg(MultipartFile file, Integer orderId) {
        if(!minioUtil.isImage(file.getOriginalFilename())){
            log.warn("{} 上传图片格式有误", orderId);
        }
        Order order = orderMapper.selectById(orderId);
        List<String> prefix = new ArrayList<>();
        prefix.add(order.getUserId().toString()+"/");
        prefix.add(order.getGoodsId().toString()+"/");
        prefix.add("Comments/");
        return minioUtil.uploadComFile(file, prefix);
    }

    @Override
    public Result delImg(String url) {
        if(minioUtil.deleteFile(url)) return Result.success();
        return Result.error("删除失败，请重试");

    }

    @Override
    public Result addCom(AddComDto addComDto) {
        Criteria criteria = Criteria.where("orderId").is(addComDto.getOrderId());
        boolean exists = mongoDbUtils.exists(criteria, Comment.class);
        if(exists) return Result.error("该订单已经完成评论，请勿重复评论");
        Order order = orderMapper.selectById(addComDto.getOrderId());
        Comment comment = new Comment();
        comment.setContent(addComDto.getContent());
        comment.setStar(addComDto.getStar());
        comment.setOrderId(addComDto.getOrderId());
        comment.setGoodsId(order.getGoodsId());
        comment.setUserId(order.getUserId());
        comment.setImages(addComDto.getImgUrl());
        Comment save = mongoDbUtils.save(comment);
        return Result.success(save);
    }

    @Override
    public Result listCom(Integer goodsId) {
        List<Comment> coms = mongoDbUtils.findCommentsByGoodsId(goodsId);
        return Result.success(coms);
    }

    @Override
    public Result updateCom(UpdateComDto updateComDto) {
        Optional<Comment> comOpt = mongoDbUtils.findById(updateComDto.getId(), Comment.class);
        Comment comment = comOpt.get();
        comment.setImages(updateComDto.getImages());
        comment.setContent(updateComDto.getContent());
        comment.setStar(updateComDto.getStar());
        comment = mongoDbUtils.save(comment);
        return Result.success(comment);
    }
}
