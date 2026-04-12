package com.vod.server.controller;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.vod.server.entity.domain.VodHistory;
import com.vod.server.entity.domain.VodVideo;
import com.vod.server.security.context.UserContext;
import com.vod.server.service.IVodHistoryService;
import com.vod.server.service.IVodVideoService;
import com.vod.server.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/app/history")
@RequiredArgsConstructor
public class VodHistoryController {

    private final IVodHistoryService vodHistoryService;
    private final IVodVideoService vodVideoService;

    /**
     * 1. 同步/上报播放进度 (前端定时触发或离开页面时触发)
     */
    @PostMapping("/sync")
    public Result<String> syncProgress(@RequestBody Map<String, Object> params) {
        Long userId = UserContext.getUserId();
        Long videoId = Long.valueOf(params.get("videoId").toString());
        // 播放到第几秒
        Integer playProgress = Integer.valueOf(params.getOrDefault("progress", 0).toString());

        // 查一下该用户以前有没有看过这个视频
        VodHistory existHistory = vodHistoryService.getOne(
                new LambdaQueryWrapper<VodHistory>()
                        .eq(VodHistory::getUserId, userId)
                        .eq(VodHistory::getVideoId, videoId)
                        .last("LIMIT 1")
        );

        if (existHistory != null) {
            // 老观众：只更新播放进度和最后观看时间 (updateTime 会由你的自动填充处理)
            VodHistory updateEntity = new VodHistory();
            updateEntity.setId(existHistory.getId());
            updateEntity.setPlayProgress(playProgress);
            vodHistoryService.updateById(updateEntity);
        } else {
            // 新观众：第一次看，做新增记录
            VodVideo video = vodVideoService.getById(videoId);
            if (video != null) {
                VodHistory newHistory = new VodHistory();
                newHistory.setUserId(userId);
                newHistory.setVideoId(videoId);
                // 冗余一点基本信息，以后查历史记录列表就不用去 Join 视频表了，提升性能
                newHistory.setVideoTitle(video.getTitle());
                newHistory.setPosterUrl(video.getPosterUrl());
                newHistory.setPlayProgress(playProgress);
                vodHistoryService.save(newHistory);
            }
        }
        return Result.success("进度同步成功");
    }

    /**
     * 2. 获取我的观看历史列表 (用于 APP "我的" -> "观看历史" 页面)
     */
    @GetMapping("/list")
    public Result<Page<VodHistory>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {

        Long userId = UserContext.getUserId();

        // 纯单表查询，直接按最后的更新时间(也就是最后一次看的时间)倒序排列
        Page<VodHistory> pageResult = vodHistoryService.page(
                new Page<>(page, size),
                new LambdaQueryWrapper<VodHistory>()
                        .eq(VodHistory::getUserId, userId)
                        .orderByDesc(VodHistory::getUpdateTime)
        );

        return Result.success(pageResult);
    }

    /**
     * 3. 清空/删除单条历史记录
     */
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        Long userId = UserContext.getUserId();

        // 加个安全校验，防止抓包删别人的历史记录
        boolean success = vodHistoryService.remove(
                new LambdaQueryWrapper<VodHistory>()
                        .eq(VodHistory::getId, id)
                        .eq(VodHistory::getUserId, userId)
        );
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
}