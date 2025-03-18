package com.project.cheerha.common.filter;

import com.project.cheerha.common.exception.handler.FilterExceptionHandler;
import com.project.cheerha.common.properties.JwtSecurityProperties;
import com.project.cheerha.domain.auth.service.BlackListService;
import com.project.cheerha.common.util.JwtUtil;
import com.project.cheerha.domain.user.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtSecurityProperties securityProperties;
    private final BlackListService blackListService;
    private final JwtUtil jwtUtil;
    private final FilterExceptionHandler filterExceptionHandler;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String url = request.getRequestURI();

        if (isWhiteList(url)) {
            chain.doFilter(request, response);
            return;
        }

        String bearerJwt = request.getHeader("Authorization");

        if (bearerJwt == null) {
            filterExceptionHandler.sendErrorResponse(response, HttpStatus.BAD_REQUEST, "JWT 토큰이 필요합니다.");
            return;
        }

        String token = jwtUtil.substringToken(bearerJwt);

        if (blackListService.isBlackList(token)) {
            filterExceptionHandler.sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "블랙리스트된 토큰입니다.");
            return;
        }

        try {
            Claims claims = jwtUtil.extractClaims(token);
            if (claims == null || claims.getSubject() == null) {
                filterExceptionHandler.sendErrorResponse(response, HttpStatus.BAD_REQUEST, "잘못된 JWT 토큰입니다.");
                return;
            }

            String[] decryptedData = claims.getSubject().split(":");
            String userId = String.valueOf(decryptedData[0]);
            Role role = decryptedData.length > 1 ? Role.valueOf(decryptedData[1]) : null;

            User userDetails = new User(userId, "", role != null ? role.getAuthorities() : null);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, token, userDetails.getAuthorities());

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // SecurityContext에 인증 정보 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);

            if (isRequiredAdmin(url) && (role == null || !role.equals(Role.ADMIN))) {
                filterExceptionHandler.sendErrorResponse(response, HttpStatus.FORBIDDEN, "관리자만 접근 가능합니다.");
                return;
            }

            chain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            log.error("Expired JWT token, 만료된 JWT token 입니다.", e);
            filterExceptionHandler.sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "만료된 JWT 토큰입니다.");
        } catch (SecurityException | MalformedJwtException e) {
            log.error("Invalid JWT signature, 유효하지 않은 JWT 서명 입니다.", e);
            filterExceptionHandler.sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "유효하지 않은 JWT 서명입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("Unsupported JWT token, 지원되지 않는 JWT 토큰 입니다.", e);
            filterExceptionHandler.sendErrorResponse(response, HttpStatus.BAD_REQUEST, "지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.error("Invalid JWT token, 유효하지 않은 JWT 토큰 입니다.", e);
            filterExceptionHandler.sendErrorResponse(response, HttpStatus.BAD_REQUEST, "잘못된 JWT 토큰 형식입니다.");
        } catch (Exception e) {
            log.error("예상치 못한 예외 발생", e);
            filterExceptionHandler.sendErrorResponse(response, HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.");
        }
    }

    private boolean isWhiteList(String requestURI) {
        return securityProperties.secret().whiteList().contains(requestURI);
    }

    private boolean isRequiredAdmin(String requestURI) {
        return securityProperties.secret().adminList().contains(requestURI);
    }
}
