package com.project.cheerha.filter;

import com.project.cheerha.common.exception.handler.FilterExceptionHandler;
import com.project.cheerha.common.security.IpBlockingFilter;
import com.project.cheerha.common.repository.KeyValueQueryRepository;
import com.project.cheerha.common.util.IpUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IpBlockingFilterTest {

    @InjectMocks
    private IpBlockingFilter ipBlockingFilter;

    @Mock
    private FilterExceptionHandler filterExceptionHandler;

    @Mock
    private KeyValueQueryRepository keyValueQueryRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private static final String TEST_IP = "127.0.0.1";
    private static final String BLOCK_KEY = "block:ip:" + TEST_IP;

    @BeforeEach
    void setUp() {
        when(IpUtil.getClientIp(request)).thenReturn(TEST_IP);
    }

    @Test
    void 차단되지않은_IP_정상처리() throws IOException, ServletException {
        when(keyValueQueryRepository.hasKey(BLOCK_KEY)).thenReturn(false);

        ipBlockingFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(filterExceptionHandler, never()).sendErrorResponse(any(), any(), anyString());
    }

    @Test
    void 차단된_IP_403_응답() throws IOException, ServletException {
        when(keyValueQueryRepository.hasKey(BLOCK_KEY)).thenReturn(true);

        ipBlockingFilter.doFilter(request, response, filterChain);

        verify(filterExceptionHandler, times(1)).sendErrorResponse(response, HttpStatus.FORBIDDEN, "30초간 차단된 IP입니다.");
        verify(filterChain, never()).doFilter(any(ServletRequest.class), any(ServletResponse.class));
    }
}
