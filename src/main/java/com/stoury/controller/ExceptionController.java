package com.stoury.controller;

import com.stoury.dto.ErrorResponse;
import com.stoury.exception.feed.FeedCreateException;
import com.stoury.exception.feed.FeedSearchException;
import com.stoury.exception.feed.FeedUpdateException;
import com.stoury.exception.member.MemberSearchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionController {
    @ExceptionHandler(value = {FeedCreateException.class, FeedUpdateException.class})
    public ResponseEntity<ErrorResponse> handle400(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.of(ex.getMessage()));
    }

    @ExceptionHandler(value = {FeedSearchException.class, MemberSearchException.class})
    public ResponseEntity<ErrorResponse> handle404(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of(ex.getMessage()));
    }
}
