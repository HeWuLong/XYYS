package com.vod.server.service.impl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.vod.server.entity.domain.VodCategory;
import com.vod.server.mapper.VodCategoryMapper;
import com.vod.server.service.VodCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VodCategoryServiceImpl extends ServiceImpl<VodCategoryMapper, VodCategory> implements VodCategoryService {
    @Override
    @Transactional
    public VodCategory getOrCreate(String categoryName) {
        LambdaQueryWrapper<VodCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VodCategory::getName, categoryName);
        VodCategory category = this.getOne(wrapper);
        if (category != null) {
            return category;
        }
        category = new VodCategory();
        category.setName(categoryName);
        category.setSortWeight(0);
        this.save(category);
        return category;
    }
}