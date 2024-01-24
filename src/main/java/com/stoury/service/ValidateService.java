package com.stoury.service;

import com.stoury.domain.Feed;
import com.stoury.domain.Member;
import com.stoury.dto.FeedCreateRequest;
import com.stoury.exception.FeedSearchException;
import com.stoury.exception.MemberCrudExceptions;
import com.stoury.repository.FeedRepository;
import com.stoury.repository.MemberRepository;
import com.stoury.utils.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ValidateService {
    private final FeedRepository feedRepository;
    private final MemberRepository memberRepository;

    public void validate(Member member) {
        Validator.of(member)
                .willCheck(m -> memberRepository.existsById(m.getId()))
                .ifFailThrowsWithMessage(MemberCrudExceptions.MemberSearchException.class, "Member not found")
                .validate();
    }

    public void validate(Feed feed) {
        Validator.of(feed)
                .willCheck(f -> feedRepository.existsById(f.getId()))
                .ifFailThrowsWithMessage(FeedSearchException.class, "Feed not found")
                .validate();
    }
}
