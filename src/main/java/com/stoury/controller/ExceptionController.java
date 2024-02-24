package com.stoury.controller;

import com.stoury.dto.ErrorResponse;
import com.stoury.exception.AlreadyLikedFeedException;
import com.stoury.exception.diary.DiaryCreateException;
import com.stoury.exception.diary.DiarySearchException;
import com.stoury.exception.feed.FeedCreateException;
import com.stoury.exception.feed.FeedSearchException;
import com.stoury.exception.feed.FeedUpdateException;
import com.stoury.exception.location.GeocodeApiException;
import com.stoury.exception.member.MemberCreateException;
import com.stoury.exception.member.MemberDeleteException;
import com.stoury.exception.member.MemberSearchException;
import com.stoury.exception.member.MemberUpdateException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class ExceptionController {
    @ExceptionHandler(value = {
            FeedCreateException.class, FeedUpdateException.class,
            AlreadyLikedFeedException.class,
            IllegalArgumentException.class,
            DiaryCreateException.class,
            MemberCreateException.class, MemberDeleteException.class, MemberUpdateException.class})
    public ResponseEntity<ErrorResponse> handle400(RuntimeException ex) {
        log.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.of(ex.getMessage()));
    }

    @ExceptionHandler(value = {FeedSearchException.class, MemberSearchException.class, DiarySearchException.class})
    public ResponseEntity<ErrorResponse> handle404(RuntimeException ex) {
        log.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse.of(ex.getMessage()));
    }

    @ExceptionHandler(value = {GeocodeApiException.class})
    public ResponseEntity<ErrorResponse> handle500(RuntimeException ex) {
        log.error(ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse.of(ex.getMessage()));
    }
}
