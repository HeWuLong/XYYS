package com.vod.server.controller;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.vod.server.utils.Result;
import com.vod.server.entity.domain.VodVideo;
import com.vod.server.service.IVodVideoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/app/video")
@RequiredArgsConstructor
@Tag(name = "APP视频接口")
public class VodVideoController {
    private final IVodVideoService vodVideoService;
    @GetMapping("/list")
    @Operation(summary = "获取视频列表（分页+分类筛选）")
    public Result<IPage<VodVideo>> list(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<VodVideo> pageResult = vodVideoService.getVideoPage(categoryId, page, size);
        return Result.success(pageResult);
    }
    @GetMapping("/detail/{id}")
    @Operation(summary = "获取视频详情")
    public Result<VodVideo> detail(@PathVariable Long id) {
        VodVideo video = vodVideoService.getVideoDetail(id);
        return video != null ? Result.success(video) : Result.error("视频不存在");
    }
}