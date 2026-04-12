package com.vod.server.task;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vod.server.config.VodSourceConfig;
import com.vod.server.entity.domain.VodCategory;
import com.vod.server.entity.domain.VodVideo;
import com.vod.server.entity.dto.VideoApiResponse;
import com.vod.server.service.IVodCategoryService;
import com.vod.server.service.IVodSourceConfigService;
import com.vod.server.service.IVodVideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class VideoSyncTask {

    //此处使用了巨量AI工具辅助，api调整太麻烦了（4.12改动：完善了后台增删api）
    private final RestTemplate restTemplate;
    private final IVodVideoService vodVideoService;
    private final IVodCategoryService vodCategoryService;
    private final IVodSourceConfigService sourceConfigService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Scheduled(cron = "0 0 3 * * ?")
    public void autoSync() {
        log.info("⏰【自动定时任务】开始执行多源采集...");
        syncVideos();
        log.info("⏰【自动定时任务】执行完毕");
    }
    public void manualSync() {
        log.info("手动触发多源采集任务...");
        syncVideos();
        log.info("手动触发采集任务结束");
    }
    private void syncVideos() {
        // 1. 查询所有状态为启用 (1) 的采集源
        List<VodSourceConfig> sourceList = sourceConfigService.list(
                new LambdaQueryWrapper<VodSourceConfig>().eq(VodSourceConfig::getStatus, 1)
        );
        if (sourceList == null || sourceList.isEmpty()) {
            log.warn("没有配置任何启用的采集源，任务结束");
            return;
        }
        // 2. 遍历每个启用的源进行采集
        for (VodSourceConfig source : sourceList) {
            log.info("🚀 开始采集资源站: {}", source.getSourceName());
            syncSingleSource(source);
        }
    }
    private void syncSingleSource(VodSourceConfig source) {
        int page = 1;
        int totalNew = 0;
        int totalUpdate = 0;
        boolean hasMore = true;
        while (hasMore) {
            try {
                // 使用数据库配置的 API 接口地址
                String url = source.getApiUrl() + "?ac=detail&pg=" + page;
                log.info("请求URL: {}", url);

                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                String jsonString = response.getBody();
                if (!StringUtils.hasText(jsonString)) {
                    log.warn("资源站 [{}] 第 {} 页返回空数据", source.getSourceName(), page);
                    break;
                }
                VideoApiResponse apiResponse = objectMapper.readValue(jsonString, VideoApiResponse.class);
                if (apiResponse.getCode() == null || apiResponse.getCode() != 1) {
                    log.error("API 返回错误码: {}, 消息: {}", apiResponse.getCode(), apiResponse.getMsg());
                    break;
                }
                List<VideoApiResponse.VideoItem> items = apiResponse.getList();
                if (items != null && !items.isEmpty()) {
                    log.info("第 {} 页获取到 {} 条视频，开始准备入库...", page, items.size());
                    for (VideoApiResponse.VideoItem item : items) {
                        try {
                            // --- 分类处理 ---
                            String typeName = item.getType_name();
                            if (!StringUtils.hasText(typeName)) {
                                typeName = "未分类";
                            }
                            VodCategory category = vodCategoryService.getOrCreate(typeName);
                            // --- 基础属性组装 ---
                            VodVideo video = new VodVideo();
                            video.setSourceId(String.valueOf(item.getVod_id()));
                            video.setTitle(item.getVod_name());
                            video.setCategoryId(category.getId());
                            video.setRegion(item.getVod_area());
                            if (StringUtils.hasText(item.getVod_year())) {
                                try {
                                    video.setPublishYear(Integer.parseInt(item.getVod_year()));
                                } catch (NumberFormatException e) {
                                    video.setPublishYear(null);
                                }
                            }
                            video.setPlayFormat("m3u8");
                            video.setPlayUrl(item.getVod_play_url());

                            // --- 海报域名处理  ---
                            String poster = item.getVod_pic();
                            if (StringUtils.hasText(poster) && !poster.startsWith("http")) {
                                String domain = source.getDomainUrl(); // 动态获取该源的域名
                                if (poster.startsWith("//")) {
                                    poster = "https:" + poster;
                                } else {
                                    poster = domain + (poster.startsWith("/") ? "" : "/") + poster;
                                }
                            }
                            video.setPosterUrl(poster);
                            // --- 扩展信息处理 ---
                            Map<String, Object> extMap = new HashMap<>();
                            extMap.put("director", item.getVod_director());
                            extMap.put("actor", item.getVod_actor());
                            extMap.put("description", item.getVod_content());
                            extMap.put("first_play_url", extractFirstPlayUrl(item.getVod_play_url()));
                            video.setExtInfo(objectMapper.writeValueAsString(extMap));

                            // --- 查重与保存逻辑 ---
                            VodVideo existVideo = vodVideoService.getOne(
                                    new LambdaQueryWrapper<VodVideo>()
                                            .eq(VodVideo::getSourceId, video.getSourceId())
                                            .last("LIMIT 1")
                            );
                            boolean exists = (existVideo != null);
                            if (exists) {
                                video.setId(existVideo.getId());
                            }
                            if (vodVideoService.saveOrUpdate(video)) {
                                if (exists) {
                                    totalUpdate++;
                                } else {
                                    totalNew++;
                                }
                            } else {
                                log.warn("保存失败: sourceId={}, title={}", video.getSourceId(), video.getTitle());
                            }
                        } catch (Exception e) {
                            log.error("处理单条视频异常，vod_id={}", item.getVod_id(), e);
                        }
                    }
                } else {
                    log.info("第 {} 页解析出的 list 为空", page);
                }
                // --- 翻页控制 ---
                Integer totalPages = apiResponse.getPagecount();
                if (totalPages != null && page < totalPages) {
                    page++;
                } else {
                    hasMore = false;
                }
                Thread.sleep(5000); // 防封IP休眠

            } catch (HttpClientErrorException.TooManyRequests e) {
                log.error("请求被限流 (429)，等待 10 秒后重试第 {} 页", page);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            } catch (Exception e) {
                log.error("采集源 [{}] 第 {} 页失败", source.getSourceName(), page, e);
                hasMore = false;
            }
        }
        log.info("✅ 资源站 [{}] 本次采集完成，新增 {} 条，更新 {} 条", source.getSourceName(), totalNew, totalUpdate);
    }
    private String extractFirstPlayUrl(String playUrlRaw) {
        if (!StringUtils.hasText(playUrlRaw)) {
            return null;
        }
        String[] episodes = playUrlRaw.split("#");
        if (episodes.length == 0) return null;
        String firstEpisode = episodes[0];
        int dollarIndex = firstEpisode.indexOf('$');
        if (dollarIndex != -1 && dollarIndex + 1 < firstEpisode.length()) {
            return firstEpisode.substring(dollarIndex + 1);
        }
        return firstEpisode;
    }
}