package com.vod.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vod.server.entity.domain.VodAd;
import com.vod.server.mapper.VodAdMapper;
import com.vod.server.service.IVodAdService;
import org.springframework.stereotype.Service;

@Service
public class VodAdServiceImpl extends ServiceImpl<VodAdMapper, VodAd> implements IVodAdService {
    //具体广告业务逻辑

}