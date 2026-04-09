package com.vod.server.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vod.server.entity.domain.VodCategory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VodCategoryMapper extends BaseMapper<VodCategory> {
}