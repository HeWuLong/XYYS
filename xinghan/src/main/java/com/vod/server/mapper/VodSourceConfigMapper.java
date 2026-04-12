package com.vod.server.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vod.server.config.VodSourceConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VodSourceConfigMapper extends BaseMapper<VodSourceConfig> {
}