package com.vod.server.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vod.server.config.VodSourceConfig;
import com.vod.server.mapper.VodSourceConfigMapper;
import com.vod.server.service.IVodSourceConfigService;
import org.springframework.stereotype.Service;

@Service
public class VodSourceConfigServiceImpl extends ServiceImpl<VodSourceConfigMapper, VodSourceConfig> implements IVodSourceConfigService {
}