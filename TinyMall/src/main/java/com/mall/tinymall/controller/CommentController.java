package com.mall.tinymall.controller;

import com.mall.tinymall.entity.dto.AddComDto;
import com.mall.tinymall.entity.dto.UpdateComDto;
import com.mall.tinymall.entity.pojo.Result;
import com.mall.tinymall.service.CommentsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * @date 2026/3/18 14:38
 * @description TODO: 商品评论控制层
 */
@RestController
@RequestMapping("/tinymall/comments")
@Tag(name="商品评论业务管理")
public class CommentController {
    @Autowired
    private CommentsService commentsService;

    @PostMapping("/uploadImg/{orderId}")
    @Operation(description = "用户写评论时添加图片")
    public Result uploadImg(MultipartFile file, @PathVariable Integer orderId){
        try{
            String imgUrl = commentsService.saveImg(file, orderId);
            return Result.success(imgUrl);
        } catch (Exception e) {
            return Result.error("上传失败，请检查图片格式后重试");
        }
    }

    @DeleteMapping("/delImg")
    @Operation(description = "用户写评论时删除图片")
    public Result delImg(@RequestBody String url){
        return commentsService.delImg(url);
    }

    @PostMapping("/addCom")
    @Operation(description = "用户写评论")
    public Result addCom(@RequestBody AddComDto addComDto){
        return commentsService.addCom(addComDto);
    }

    @GetMapping("/{goodsId}")
    @Operation(description = "进入商品页后加载评论")
    public Result goodsComments(@PathVariable Integer goodsId){
        return commentsService.listCom(goodsId);
    }

    @PutMapping("/updateCom")
    @Operation(description = "用户修改评论")
    public Result updateCom(@RequestBody UpdateComDto updateComDto){
        return commentsService.updateCom(updateComDto);
    }



}
