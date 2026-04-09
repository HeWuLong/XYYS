package com.vod.server.task;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vod.server.service.IVodVideoService;
import org.springframework.scheduling.annotation.Scheduled;
import com.vod.server.entity.domain.VodCategory;
import com.vod.server.entity.domain.VodVideo;
import com.vod.server.entity.dto.VideoApiResponse;
import com.vod.server.service.VodCategoryService;
import com.vod.server.service.VodVideoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class VideoSyncTask {
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    private IVodVideoService vodVideoService;
    @Autowired
    private VodCategoryService vodCategoryService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Scheduled(cron = "0 0 3 * * ?")
    public void autoSync() {
        log.info("⏰【自动定时任务】开始执行采集...");
        syncVideos();
        log.info("⏰【自动定时任务】执行完毕");
    }
    public void manualSync() {
        log.info("手动触发采集任务...");
        syncVideos();
        log.info("手动触发采集任务结束");
    }

    private void syncVideos() {
        int page = 1;
        int totalNew = 0;
        int totalUpdate = 0;
        boolean hasMore = true;
        while (hasMore) {
            try {
                String baseUrl = "https://caiji.maotaizy.cc/api.php/provide/vod/at/json/";
                String url = baseUrl + "?ac=detail&pg=" + page;
                log.info("请求URL: {}", url);

                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                String jsonString = response.getBody();
                if (jsonString == null || jsonString.trim().isEmpty()) {
                    log.warn("第 {} 页返回空数据", page);
                    break;
                }

                // 调试：打印原始 JSON 前500字符
                log.info("API 返回原始字符串: {}", jsonString.substring(0, Math.min(500, jsonString.length())));

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
                            if (typeName == null || typeName.trim().isEmpty()) {
                                typeName = "未分类";
                            }
                            VodCategory category = vodCategoryService.getOrCreate(typeName);
                            VodVideo video = new VodVideo();
                            video.setSourceId(String.valueOf(item.getVod_id()));
                            video.setTitle(item.getVod_name());
                            video.setCategoryId(category.getId());
                            video.setRegion(item.getVod_area());
                            if (item.getVod_year() != null && !item.getVod_year().isEmpty()) {
                                try {
                                    video.setPublishYear(Integer.parseInt(item.getVod_year()));
                                } catch (NumberFormatException e) {
                                    video.setPublishYear(null);
                                }
                            }
                            video.setPlayFormat("m3u8");
                            video.setPlayUrl(item.getVod_play_url());
                            String poster = item.getVod_pic();
                            if (poster != null && !poster.startsWith("http")) {
                                String domain = "https://caiji.maotaizy.cc"; // 这里后续可以写进配置
                                if (poster.startsWith("/")) {
                                    poster = domain + poster;
                                } else {
                                    poster = domain + "/" + poster;
                                }
                            }
                            video.setPosterUrl(poster);
                            Map<String, Object> extMap = new HashMap<>();
                            extMap.put("director", item.getVod_director());
                            extMap.put("actor", item.getVod_actor());
                            extMap.put("description", item.getVod_content());
                            String firstPlayUrl = extractFirstPlayUrl(item.getVod_play_url());
                            extMap.put("first_play_url", firstPlayUrl);
                            video.setExtInfo(objectMapper.writeValueAsString(extMap));
                            VodVideo existVideo = vodVideoService.getOne(
                                    new LambdaQueryWrapper<VodVideo>()
                                            .eq(VodVideo::getSourceId, video.getSourceId())
                                            .last("LIMIT 1") // 万一有重复数据，只取第一条，防止报错
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
                Integer totalPages = apiResponse.getPagecount();
                if (totalPages != null && page < totalPages) {
                    page++;
                } else {
                    hasMore = false;
                }
                Thread.sleep(3000);
            } catch (HttpClientErrorException.TooManyRequests e) {
                log.error("请求被限流 (429)，等待 10 秒后重试第 {} 页", page);
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            } catch (Exception e) {
                log.error("采集第 {} 页失败", page, e);
                hasMore = false;
            }
        }
        log.info("本次采集完成，新增 {} 条，更新 {} 条", totalNew, totalUpdate);
    }
    private String extractFirstPlayUrl(String playUrlRaw) {
        if (playUrlRaw == null || playUrlRaw.isEmpty()) {
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