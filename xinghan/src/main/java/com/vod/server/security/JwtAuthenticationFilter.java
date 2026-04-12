package com.vod.server.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vod.server.exception.ErrorCodeEnum;
import com.vod.server.exception.ServiceException;
import com.vod.server.security.context.UserContext;
import com.vod.server.utils.JwtUtil;
import com.vod.server.utils.Result;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        return path.equals("/app/login")
                || path.equals("/app/register")
                || path.equals("/app/category/list")
                || path.equals("/app/video/list")
                || path.equals("/app/video/banner")
                || path.equals("/app/video/recommend")
                || path.startsWith("/app/video/detail/")
                || path.startsWith("/public/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                throw new ServiceException(ErrorCodeEnum.UNAUTHORIZED, "缺少或无效的 Authorization 头");
            }

            String token = authHeader.substring(7);
            if (!jwtUtil.validateToken(token)) {
                throw new ServiceException(ErrorCodeEnum.UNAUTHORIZED, "Token 无效或已过期");
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            Boolean isAdmin = jwtUtil.getIsAdminFromToken(token);

            UserContext.setUserId(userId);
            UserContext.setIsAdmin(isAdmin);

            List<SimpleGrantedAuthority> authorities = Boolean.TRUE.equals(isAdmin)
                    ? List.of(new SimpleGrantedAuthority("ROLE_ADMIN"), new SimpleGrantedAuthority("ROLE_USER"))
                    : Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(String.valueOf(userId), null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (ServiceException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            Result<Object> errorResult = Result.error(e.getErrorCode().getCode(), e.getMessage());
            response.getWriter().write(objectMapper.writeValueAsString(errorResult));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("application/json;charset=UTF-8");
            Result<Object> errorResult = Result.error(ErrorCodeEnum.SYSTEM_ERROR.getCode(), "认证过程发生错误: " + e.getMessage());
            response.getWriter().write(objectMapper.writeValueAsString(errorResult));
        } finally {
            SecurityContextHolder.clearContext();
            UserContext.clear();
        }
    }
}
