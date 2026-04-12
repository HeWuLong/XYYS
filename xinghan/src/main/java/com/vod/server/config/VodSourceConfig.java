package com.vod.server.config;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.net.URI;
import java.time.LocalDateTime;

@Data
@TableName("vod_source")
public class VodSourceConfig {
    @TableId(type = IdType.AUTO)
    private Long id;

    // DB: vod_source.name
    @TableField("name")
    private String sourceName;

    // DB: vod_source.api_url
    @TableField("api_url")
    private String apiUrl;

    // DB: vod_source.parse_type
    @TableField("parse_type")
    private Integer parseType;

    // DB: vod_source.sort_weight
    @TableField("sort_weight")
    private Integer sortWeight;

    // DB: vod_source.status
    private Integer status;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 仅前端展示/传参，不落库
    @TableField(exist = false)
    private String domainUrl;

    public String getDomainUrl() {
        if (domainUrl != null && !domainUrl.isBlank()) {
            return domainUrl;
        }
        if (apiUrl == null || apiUrl.isBlank()) {
            return "";
        }
        try {
            URI uri = URI.create(apiUrl);
            if (uri.getScheme() == null || uri.getHost() == null) {
                return "";
            }
            return uri.getScheme() + "://" + uri.getHost();
        } catch (Exception e) {
            return "";
        }
    }
}
