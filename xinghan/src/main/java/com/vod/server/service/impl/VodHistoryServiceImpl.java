package com.vod.server.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vod.server.entity.domain.VodHistory;
import com.vod.server.mapper.VodHistoryMapper;
import com.vod.server.service.IVodHistoryService;
import org.springframework.stereotype.Service;

@Service
public class VodHistoryServiceImpl extends ServiceImpl<VodHistoryMapper, VodHistory> implements IVodHistoryService {
    // 这里已经自动拥有了 baseMapper 和所有 CRUD 的实现
}