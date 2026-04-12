package com.vod.server.mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.vod.server.entity.domain.VodHistory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface VodHistoryMapper extends BaseMapper<VodHistory> {
    // 必须要带上 <VodHistory>
}
