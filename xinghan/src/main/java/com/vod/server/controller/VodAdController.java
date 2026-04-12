package com.vod.server.controller;

import com.vod.server.entity.domain.VodAd;
import com.vod.server.service.IVodAdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ad")
public class VodAdController {
    @Autowired
    private IVodAdService vodAdService;
    // 1. 获取所有广告 (GET /api/ad/list)
    @GetMapping("/list")
    public List<VodAd> listAds() {
        return vodAdService.list();
    }
    // 2. 新增广告 (POST /api/ad/save)
    @PostMapping("/save")
    public boolean saveAd(@RequestBody VodAd vodAd) {
        // 自动填充
        return vodAdService.save(vodAd);
    }
    // 3. 修改广告 (PUT /api/ad/update)
    @PutMapping("/update")
    public boolean updateAd(@RequestBody VodAd vodAd) {
        return vodAdService.updateById(vodAd);
    }
    // 4. 删除广告 (DELETE /api/ad/delete/1)
    @DeleteMapping("/delete/{id}")
    public boolean deleteAd(@PathVariable Long id) {
        // 标记删除
        return vodAdService.removeById(id);
    }
}