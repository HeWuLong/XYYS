package com.vod.server.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vod.server.entity.domain.VodVideo;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VodVideoMapper extends BaseMapper<VodVideo> {
}