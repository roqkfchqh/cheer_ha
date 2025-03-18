package com.project.cheerha.domain.auth.service;

import com.project.cheerha.common.email.sender.VerificationEmailSender;
import com.project.cheerha.common.exception.auth.AuthErrorCode;
import com.project.cheerha.common.exception.auth.UnAuthorizedException;
import com.project.cheerha.common.exception.client.BadRequestException;
import com.project.cheerha.common.exception.client.ClientErrorCode;
import com.project.cheerha.common.properties.JwtSecurityProperties;
import com.project.cheerha.domain.user.service.EmailTokenService;
import com.project.cheerha.common.util.JwtUtil;
import com.project.cheerha.domain.auth.dto.request.CreateLoginRequestDto;
import com.project.cheerha.domain.auth.dto.request.CreateSignupRequestDto;
import com.project.cheerha.domain.auth.dto.request.VerifySignupRequestDto;
import com.project.cheerha.domain.auth.dto.response.*;
import com.project.cheerha.domain.user.entity.User;
import com.project.cheerha.domain.user.repository.UserRepository;
import com.project.cheerha.domain.user.service.UserFindByService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtSecurityProperties jwtSecurityProperties;
    private final RefreshTokenService refreshTokenService;
    private final BlackListService blackListService;
    private final UserFindByService userFindByService;
    private final VerificationEmailSender verificationEmailSender;
    private final EmailTokenService emailTokenService;

    public static final String SIGNUP_TOKEN_PREFIX = "signup_email_verification_token";

    /**
     * 회원가입 처리하는 메서드
     * @throws BadRequestException 이메일이 이미 존재하는 경우
     */
    public CreateSignupResponseDto signup(CreateSignupRequestDto dto) {
        if (userRepository.existsByEmail(dto.email())) {
            throw new BadRequestException(ClientErrorCode.ALREADY_EXIST_EMAIL);
        }
        String token = emailTokenService.saveToken(SIGNUP_TOKEN_PREFIX, dto.email());
        verificationEmailSender.sendVerificationEmail(dto.email(), token);
        return CreateSignupResponseDto.toDto();
    }

    public VerifySignupResponseDto verifySignup(VerifySignupRequestDto dto) {
        emailTokenService.verifyEmailToken(SIGNUP_TOKEN_PREFIX, dto.email(), dto.token());
        String encodedPassword = passwordEncoder.encode(dto.password());

        User user = User.toEntity(
                dto.email(),
                dto.name(),
                dto.age(),
                dto.career(),
                encodedPassword
        );
        userRepository.save(user);
        return VerifySignupResponseDto.toDto();
    }

    /**
     * 로그인 메서드 - AccessToken 과 RefreshToken 생성
     * @return 로그인 응답 객체(AccessToken, RefreshToken 포함)
     */
    public CreateLoginResponseDto login(CreateLoginRequestDto dto) {
        User user = userFindByService.findByEmail(dto.email());
        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new UnAuthorizedException(AuthErrorCode.INVALID_PASSWORD);
        }

        String accessToken = jwtUtil.createToken(user.getId(), user.getRole());
        String refreshToken = jwtUtil.createRefreshToken(user.getId());

        refreshTokenService.createRefreshToken(user.getId(), refreshToken);

        return CreateLoginResponseDto.toDto(accessToken, refreshToken);
    }

    /**
     * 로그아웃 메서드 - AccessToken 을 BlackList 에 추가하고, RefreshToken 을 삭제
     * @param authHeader 인증 헤더(prefix 포함된 token)
     * @throws UnAuthorizedException 토큰이 유효하지 않은 경우
     */
    public CreateLogoutResponseDto logout(String authHeader) {
        String prefix = jwtSecurityProperties.token().prefix();
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(prefix)) {
            throw new UnAuthorizedException(AuthErrorCode.TOKEN_UNAUTHORIZED);
        }
        String token = jwtUtil.substringToken(authHeader);
        Claims claims = jwtUtil.extractClaims(token);
        long expirationMillis = claims.getExpiration().getTime() - System.currentTimeMillis();

        if (expirationMillis > 0) {
            blackListService.addToBlackList(token);
        }
        String[] accessTokenData = claims.getSubject().split(":");
        Long userId = Long.valueOf(accessTokenData[0]);

        refreshTokenService.deleteRefreshToken(userId);

        return CreateLogoutResponseDto.toDto();
    }

    /**
     * 새로운 AccessToken 을 발급하는 메서드, 사용된 RefreshToken 도 재발급
     * @param refreshToken 현재 사용자의 RefreshToken
     * @return 새로운 AccessToken
     * @throws UnAuthorizedException 토큰이 유효하지 않거나 - 저장된 값과 다를 경우
     */
    public RefreshAccessTokenResponseDto refreshAccessToken(String refreshToken) {
        refreshToken = jwtUtil.substringToken(refreshToken);

        Claims claims;

        try {
            claims = jwtUtil.extractClaims(refreshToken);
        } catch (Exception e) {
            throw new UnAuthorizedException(AuthErrorCode.TOKEN_UNAUTHORIZED);
        }
        Long userId = Long.parseLong(claims.getSubject());

        String storedRefreshToken = refreshTokenService.getRefreshToken(userId);

        if (!refreshToken.equals(jwtUtil.substringToken(storedRefreshToken))) {
            throw new UnAuthorizedException(AuthErrorCode.TOKEN_UNAUTHORIZED);
        }

        String newRefreshToken = jwtUtil.createRefreshToken(userId);
        refreshTokenService.createRefreshToken(userId, newRefreshToken);

        User user = userFindByService.findById(userId);

        String refreshAccessToken = jwtUtil.createToken(userId, user.getRole());
        return RefreshAccessTokenResponseDto.toDto(refreshAccessToken, newRefreshToken);
    }
}
