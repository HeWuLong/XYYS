package com.vod.server.service;
import com.baomidou.mybatisplus.extension.service.IService;
import com.vod.server.entity.domain.VodVideo;

public interface VodVideoService extends IService<VodVideo> {
    VodVideo getBySourceId(String sourceId);
    boolean saveOrUpdateBySourceId(VodVideo video);
}