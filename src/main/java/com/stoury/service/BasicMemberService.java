package com.stoury.service;

import com.stoury.domain.Member;
import com.stoury.dto.MemberCreateRequest;
import com.stoury.dto.MemberResponse;
import com.stoury.exception.MemberCreateException;
import com.stoury.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class BasicMemberService implements MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public MemberResponse createMember(MemberCreateRequest memberCreateRequest) {
        validateRequestMember(memberCreateRequest);
        String encryptedPassword = passwordEncoder.encode(memberCreateRequest.password());

        Member member = Member.builder()
                .email(memberCreateRequest.email())
                .username(memberCreateRequest.username())
                .encryptedPassword(encryptedPassword)
                .introduction(memberCreateRequest.introduction())
                .build();

        Member newMember = memberRepository.save(member);

        return MemberResponse.from(newMember);
    }

    private void validateRequestMember(MemberCreateRequest memberCreateRequest) {
        String email = memberCreateRequest.email();
        String username = memberCreateRequest.username();
        String password = memberCreateRequest.password();

        if (validateEmail(email) && validateUserName(username) && validatePassword(password)) {
            return;
        }
        throw new MemberCreateException();
    }

    private boolean validatePassword(String password) {
        return StringUtils.hasText(password)
                && password.length() >= 8 && password.length() <= 30;
    }

    private boolean validateUserName(String username) {
        return StringUtils.hasText(username) && username.length() <= 10;
    }

    private boolean validateEmail(String email) {
        return StringUtils.hasText(email) && email.length() <= 25;
    }
}
