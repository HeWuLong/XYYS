package com.vod.server.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vod.server.entity.domain.VodVideo;
import com.vod.server.mapper.VodVideoMapper;
import com.vod.server.service.IVodVideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class VodVideoServiceImpl extends ServiceImpl<VodVideoMapper, VodVideo> implements IVodVideoService {
    @Override
    public IPage<VodVideo> getVideoPage(Long categoryId, Integer page, Integer size) {
        Page<VodVideo> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<VodVideo> wrapper = new LambdaQueryWrapper<>();
        if (categoryId != null) {
            wrapper.eq(VodVideo::getCategoryId, categoryId);
        }
        wrapper.orderByDesc(VodVideo::getPublishYear)
                .orderByDesc(VodVideo::getCreateTime);
        wrapper.select(VodVideo::getId,
                VodVideo::getTitle,
                VodVideo::getPosterUrl,
                VodVideo::getPublishYear);
        return baseMapper.selectPage(pageParam, wrapper);
    }
    @Override
    public VodVideo getVideoDetail(Long id) {
        // 根据 ID 查询完整数据
        return baseMapper.selectById(id);
    }
}