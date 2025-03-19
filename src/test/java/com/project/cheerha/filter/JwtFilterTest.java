package com.project.cheerha.filter;

import com.project.cheerha.common.exception.handler.FilterExceptionHandler;
import com.project.cheerha.common.security.JwtAuthenticationFilter;
import com.project.cheerha.common.properties.JwtSecurityProperties;
import com.project.cheerha.domain.auth.service.BlackListService;
import com.project.cheerha.common.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtFilterTest {

    @Mock
    private BlackListService blackListService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private FilterExceptionHandler filterExceptionHandler;

    @Mock
    private JwtSecurityProperties jwtSecurityProperties;

    @InjectMocks
    private JwtAuthenticationFilter jwtFilter;

    @Test
    void testFilter_화이트리스트일때() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/auth/signup");
        JwtSecurityProperties.Secret mockSecret = mock(JwtSecurityProperties.Secret.class);

        setUpJwtProperties(mockSecret);
        jwtFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testFilter_토큰값이_비어있을때() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        request.setRequestURI("/api/protected");
        request.addHeader("Authorization", "");
        JwtSecurityProperties.Secret mockSecret = mock(JwtSecurityProperties.Secret.class);

        setUpJwtProperties(mockSecret);
        jwtFilter.doFilter(request, response, filterChain);

        verify(filterExceptionHandler, times(1))
                .sendErrorResponse(any(HttpServletResponse.class), eq(HttpStatus.BAD_REQUEST), anyString());
    }

    @Test
    void testFilter_토큰값이_정상일때() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn("Bearer validToken");
        when(jwtUtil.substringToken("Bearer validToken")).thenReturn("validToken");
        when(blackListService.isBlackList("validToken")).thenReturn(false);

        Claims claims = mock(Claims.class);
        when(jwtUtil.extractClaims("validToken")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("1:USER");

        JwtSecurityProperties securityProperties = mock(JwtSecurityProperties.class);

        jwtFilter = new JwtAuthenticationFilter(blackListService, jwtUtil, filterExceptionHandler);
        JwtSecurityProperties.Secret mockSecret = mock(JwtSecurityProperties.Secret.class);
        when(securityProperties.secret()).thenReturn(mockSecret);
        when(mockSecret.whiteList()).thenReturn(List.of("/auth/signup", "/auth/login"));
        jwtFilter.doFilter(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void testFilter_만료된_토큰일때() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn("Bearer expiredToken");
        when(jwtUtil.substringToken("Bearer expiredToken")).thenReturn("expiredToken");
        when(jwtUtil.extractClaims("expiredToken")).thenThrow(ExpiredJwtException.class);

        JwtSecurityProperties.Secret mockSecret = mock(JwtSecurityProperties.Secret.class);

        setUpJwtProperties(mockSecret);
        jwtFilter.doFilter(request, response, filterChain);

        verify(filterExceptionHandler, times(1))
                .sendErrorResponse(any(HttpServletResponse.class), eq(HttpStatus.UNAUTHORIZED), anyString());
    }

    @Test
    void testFilter_블랙리스트_토큰일때() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn("Bearer blackListToken");
        when(jwtUtil.substringToken("Bearer blackListToken")).thenReturn("blackListToken");

        when(blackListService.isBlackList("blackListToken")).thenReturn(true);

        JwtSecurityProperties.Secret mockSecret = mock(JwtSecurityProperties.Secret.class);

        setUpJwtProperties(mockSecret);
        jwtFilter.doFilter(request, response, filterChain);

        verify(filterExceptionHandler, times(1))
                .sendErrorResponse(any(HttpServletResponse.class), eq(HttpStatus.UNAUTHORIZED), anyString());
    }

    @Test
    void testFilter_Authorization_헤더가_없을때() throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain filterChain = mock(FilterChain.class);

        request.setRequestURI("/api/protected");

        JwtSecurityProperties.Secret mockSecret = mock(JwtSecurityProperties.Secret.class);

        setUpJwtProperties(mockSecret);
        jwtFilter.doFilter(request, response, filterChain);

        verify(filterExceptionHandler, times(1))
                .sendErrorResponse(any(HttpServletResponse.class), eq(HttpStatus.BAD_REQUEST), anyString());
    }

    @Test
    void testFilter_유저정보가_없는_토큰일때() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn("Bearer validToken");
        when(jwtUtil.substringToken("Bearer validToken")).thenReturn("validToken");
        when(blackListService.isBlackList("validToken")).thenReturn(false);

        Claims claims = mock(Claims.class);
        when(jwtUtil.extractClaims("validToken")).thenReturn(claims);

        when(claims.getSubject()).thenReturn(null);

        JwtSecurityProperties.Secret mockSecret = mock(JwtSecurityProperties.Secret.class);

        setUpJwtProperties(mockSecret);
        jwtFilter.doFilter(request, response, filterChain);

        verify(filterExceptionHandler, times(1))
                .sendErrorResponse(any(HttpServletResponse.class), eq(HttpStatus.BAD_REQUEST), anyString());
    }

    @Test
    void testFilter_유효하지_않은_권한일때() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/protected");
        when(request.getHeader("Authorization")).thenReturn("Bearer validToken");
        when(jwtUtil.substringToken("Bearer validToken")).thenReturn("validToken");
        when(blackListService.isBlackList("validToken")).thenReturn(false);

        when(jwtUtil.extractClaims("validToken"))
                .thenThrow(new IllegalArgumentException("Invalid or expired JWT token"));

        JwtSecurityProperties.Secret mockSecret = mock(JwtSecurityProperties.Secret.class);

        setUpJwtProperties(mockSecret);
        jwtFilter.doFilter(request, response, filterChain);

        verify(filterExceptionHandler, times(1))
                .sendErrorResponse(any(HttpServletResponse.class), eq(HttpStatus.BAD_REQUEST), anyString());
    }

    private void setUpJwtProperties(JwtSecurityProperties.Secret mockSecret) {
        when(jwtSecurityProperties.secret()).thenReturn(mockSecret);
        when(mockSecret.whiteList()).thenReturn(List.of(
                "/auth/signup",
                "/auth/signup-verify",
                "/auth/login",
                "/actuator/health",
                "/auth/refresh"
        ));
        jwtFilter = new JwtAuthenticationFilter(blackListService, jwtUtil, filterExceptionHandler);
    }
}
