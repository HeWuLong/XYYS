package com.vod.server.controller;
import com.vod.server.entity.domain.VodCategory;
import com.vod.server.service.IVodCategoryService;
import com.vod.server.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/app/category")
@RequiredArgsConstructor
public class VodCategoryController {
    private final IVodCategoryService vodCategoryService;

    @GetMapping("/list")
    public Result<List<VodCategory>> list() {
        List<VodCategory> categories = vodCategoryService.list();
        return Result.success(categories);
    }
}