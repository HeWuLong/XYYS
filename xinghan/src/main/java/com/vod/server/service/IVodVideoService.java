package com.vod.server.service;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vod.server.entity.domain.VodVideo;
import lombok.Data;

import java.util.List;

public interface IVodVideoService extends IService<VodVideo> {
    IPage<VodVideo> getVideoPage(Long categoryId, String type, String region, String year, String sort, Integer page, Integer size);

    List<VodVideo> getRecommendations(Long currentVideoId, Long categoryId);

    VodVideo getVideoDetail(Long id);
}