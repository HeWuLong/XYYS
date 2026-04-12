package com.vod.server.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vod.server.entity.domain.VodCategory;
import com.vod.server.mapper.VodCategoryMapper;
import com.vod.server.service.IVodCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class VodCategoryServiceImpl extends ServiceImpl<VodCategoryMapper, VodCategory> implements IVodCategoryService {
    @Override
    public VodCategory getOrCreate(String categoryName) {
        if (!StringUtils.hasText(categoryName)) {
            categoryName = "未分类";
        }
        VodCategory category = this.getOne(
                new LambdaQueryWrapper<VodCategory>()
                        .eq(VodCategory::getName, categoryName) // 假设你的表里分类名称字段叫 name
                        .last("LIMIT 1")
        );
        if (category == null) {
            category = new VodCategory();
            category.setName(categoryName);
            this.save(category);
        }
        return category;
    }
}