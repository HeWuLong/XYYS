package com.vod.server.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();
        // 允许跨域携带cookie
        config.setAllowCredentials(true);
        // 允许所有域名跨域调用
        config.addAllowedOriginPattern("*");
        // 允许所有请求头
        config.addAllowedHeader("*");
        // 允许所有请求方法 (GET, POST, PUT, DELETE, OPTIONS等)
        config.addAllowedMethod("*");
        // 对所有接口都生效
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}