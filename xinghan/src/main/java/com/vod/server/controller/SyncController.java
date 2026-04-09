package com.vod.server.controller;
import com.vod.server.task.VideoSyncTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/sync")
public class SyncController {
    @Autowired
    private VideoSyncTask videoSyncTask;
    @PostMapping("/video")
    public Map<String, Object> syncVideo() {
        long start = System.currentTimeMillis();
        videoSyncTask.manualSync();   // 您已有的方法
        long cost = System.currentTimeMillis() - start;
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("costMs", cost);
        result.put("message", "同步任务执行完成");
        return result;
    }
}