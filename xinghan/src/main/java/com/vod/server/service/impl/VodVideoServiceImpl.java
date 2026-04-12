package com.vod.server.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vod.server.entity.domain.VodVideo;
import com.vod.server.mapper.VodVideoMapper;
import com.vod.server.security.context.UserContext;
import com.vod.server.service.IVodVideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.List;


@Service
@RequiredArgsConstructor
public class VodVideoServiceImpl extends ServiceImpl<VodVideoMapper, VodVideo> implements IVodVideoService {
    @Override
    public IPage<VodVideo> getVideoPage(Long categoryId, String type, String region, String year, String sort, Integer page, Integer size) {
        Page<VodVideo> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<VodVideo> wrapper = new LambdaQueryWrapper<>();
        // 1. 大类筛选
        if (categoryId != null) {
            wrapper.eq(VodVideo::getCategoryId, categoryId);
        }
        // 2. 动态多维条件筛选 (判空并拼接 SQL)
        if (StringUtils.hasText(type)) {
            wrapper.like(VodVideo::getSubCategory, type); // 模糊匹配小类
        }
        if (StringUtils.hasText(region)) {
            wrapper.eq(VodVideo::getRegion, region);
        }
        if (StringUtils.hasText(year)) {
            wrapper.eq(VodVideo::getPublishYear, Integer.valueOf(year));
        }

        // 3. 动态排序
        if ("最热".equals(sort)) {
            wrapper.orderByDesc(VodVideo::getId); // 暂时用 id 降序模拟热度排序
        } else if ("评分".equals(sort)) {
            wrapper.orderByDesc(VodVideo::getId); // 暂时用 id 降序模拟评分排序
        } else {
            // 默认 "最新" 和 "综合"
            wrapper.orderByDesc(VodVideo::getPublishYear)
                    .orderByDesc(VodVideo::getCreateTime);
        }
        // 4. 指定查询字段 (性能优化：只查列表需要的字段，不查 play_url 这种大文本)
        wrapper.select(
                VodVideo::getId,
                VodVideo::getTitle,
                VodVideo::getPosterUrl,
                VodVideo::getPublishYear,
                VodVideo::getRegion,
                VodVideo::getSubCategory,
                VodVideo::getUpdateStatus,
                VodVideo::getEpisodeCount
        );

        return baseMapper.selectPage(pageParam, wrapper);
    }
    @Override
    public List<VodVideo> getRecommendations(Long currentVideoId, Long categoryId) {
        Long currentUserId = UserContext.getUserId();
        // TODO: 4.12:V2.0 优化点 - 这里预留了接入大数据推荐算法的空间
        //  V1.0 基础降级策略
        LambdaQueryWrapper<VodVideo> queryWrapper = new LambdaQueryWrapper<>();
        //  如果用户正在看某部片，优先推荐同分类下的其他片子
        if (categoryId != null) {
            queryWrapper.eq(VodVideo::getCategoryId, categoryId);
        }
        // 排除当前正在看的这部片
        if (currentVideoId != null) {
            queryWrapper.ne(VodVideo::getId, currentVideoId);
        }
        // 2. 兜底：按热度（比如播放次数，目前没这个字段先用时间代替）倒序，随机取 6 条
        queryWrapper.orderByDesc(VodVideo::getCreateTime).last("LIMIT 6");
        return this.list(queryWrapper);
    }
    @Override
    public VodVideo getVideoDetail(Long id) {
        return baseMapper.selectById(id);
    }
}