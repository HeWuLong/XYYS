package com.vod.server.service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vod.server.entity.domain.VodVideo;

public interface IVodVideoService extends IService<VodVideo> {

    IPage<VodVideo> getVideoPage(Long categoryId, Integer page, Integer size);
    VodVideo getVideoDetail(Long id);
}