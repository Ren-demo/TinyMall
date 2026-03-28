package com.mall.tinymall.service;

import com.mall.tinymall.entity.dto.AddComDto;
import com.mall.tinymall.entity.dto.UpdateComDto;
import com.mall.tinymall.entity.pojo.Result;
import org.springframework.web.multipart.MultipartFile;

public interface CommentsService {
    String saveImg(MultipartFile file, Integer orderId);

    Result delImg(String url);

    Result addCom(AddComDto addComDto);

    Result listCom(Integer goodsId);

    Result updateCom(UpdateComDto updateComDto);
}
