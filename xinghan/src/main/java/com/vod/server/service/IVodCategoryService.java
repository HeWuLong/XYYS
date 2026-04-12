package com.vod.server.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vod.server.entity.domain.VodCategory;

public interface IVodCategoryService extends IService<VodCategory> {
    VodCategory getOrCreate(String categoryName);
}