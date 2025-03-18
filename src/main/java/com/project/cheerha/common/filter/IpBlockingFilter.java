package com.project.cheerha.common.filter;

import com.project.cheerha.common.exception.handler.FilterExceptionHandler;
import com.project.cheerha.common.repository.KeyValueQueryRepository;
import com.project.cheerha.common.util.IpUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class IpBlockingFilter extends OncePerRequestFilter {

    private final FilterExceptionHandler filterExceptionHandler;
    private final KeyValueQueryRepository keyValueQueryRepository;

    private static final String BLOCK_PREFIX = "block:ip:";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String ip = IpUtil.getClientIp(request);
        String redisBlockKey = BLOCK_PREFIX + ip;

        if (Boolean.TRUE.equals(keyValueQueryRepository.hasKey(redisBlockKey))) {
            log.warn("차단된 IP 로그인 시도: {}", ip);
            filterExceptionHandler.sendErrorResponse(response, HttpStatus.FORBIDDEN, "30초간 차단된 IP입니다.");
            return;
        }

        chain.doFilter(request, response);
    }
}
