package com.project.cheerha.domain.user.service;

import com.project.cheerha.common.exception.client.BadRequestException;
import com.project.cheerha.common.exception.client.ClientErrorCode;
import com.project.cheerha.common.exception.data.DataErrorCode;
import com.project.cheerha.common.exception.data.NotFoundException;
import com.project.cheerha.domain.auth.repository.BannedEmailRepository;
import com.project.cheerha.domain.user.dto.request.ResetPasswordRequestDto;
import com.project.cheerha.domain.user.dto.request.UpdatePasswordRequestDto;
import com.project.cheerha.domain.user.dto.response.UpdatePasswordResponseDto;
import com.project.cheerha.domain.user.entity.User;
import com.project.cheerha.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserPasswordService {

    private final UserFindByService userFindByService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final BannedEmailRepository bannedEmailRepository;
    private final EmailTokenService emailTokenService;

    /**
     * 로그인 사용자의 패스워드 업데이트
     */
    @Transactional
    public UpdatePasswordResponseDto updatePassword(Long id, UpdatePasswordRequestDto requestDto) {
        User user = userFindByService.findById(id);
        if(!passwordEncoder.matches(requestDto.oldPassword(), user.getPassword())) {
            throw new BadRequestException(ClientErrorCode.INVALID_CURRENT_PASSWORD);
        }
        user.updatePassword(passwordEncoder.encode(requestDto.newPassword()));
        return UpdatePasswordResponseDto.toDto();
    }

    /**
     * 비로그인 사용자의 패스워드 리셋 또는 이메일 밴 삭제
     * @param requestDto 이메일, 토큰, 새 비밀번호
     */
    @Transactional
    public UpdatePasswordResponseDto resetPassword(ResetPasswordRequestDto requestDto) {
        if(!userRepository.existsByEmail(requestDto.email())) {
            throw new NotFoundException(DataErrorCode.USER_NOT_FOUND);
        }
        emailTokenService.verifyPasswordResetToken(requestDto.email(), requestDto.token());

        User user = userFindByService.findByEmail(requestDto.email());
        user.updatePassword(passwordEncoder.encode(requestDto.newPassword()));

        if (bannedEmailRepository.existsByEmail(requestDto.email())) {
            bannedEmailRepository.deleteByEmail(requestDto.email());
        }
        return UpdatePasswordResponseDto.toDto();
    }
}
