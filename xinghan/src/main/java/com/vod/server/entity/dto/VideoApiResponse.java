package com.vod.server.entity.dto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class VideoApiResponse {
    private Integer code;
    private String msg;
    private List<VideoItem> list;
    private Integer page;
    private Integer pagecount;
    private Integer total;
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class VideoItem {
        private Integer vod_id;
        private String vod_name;
        private String vod_pic;
        private String vod_play_url;
        private String type_name;
        private String vod_year;
        private String vod_area;
        private String vod_director;
        private String vod_actor;
        private String vod_content;
    }
}