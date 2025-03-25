package com.project.cheerha.common.exception.server;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ServerErrorCode {

    //EncryptException
    TOKEN_ENCRYPTION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "토큰 암호화에 실패하였습니다.");

    private final HttpStatus status;
    private final String message;
}
