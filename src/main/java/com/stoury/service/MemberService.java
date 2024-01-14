package com.stoury.service;

import com.stoury.domain.Member;
import com.stoury.dto.MemberCreateRequest;
import com.stoury.dto.MemberResponse;
import com.stoury.exception.MemberCreateException;
import com.stoury.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment env;

    public MemberResponse createMember(MemberCreateRequest memberCreateRequest) {
        validateRequestMember(memberCreateRequest);
        String encryptedPassword = passwordEncoder.encode(memberCreateRequest.password());

        Member member = memberCreateRequest.toEntity(encryptedPassword);

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
                && password.length() >= Integer.parseInt(env.getProperty("member.password.length.min"))
                && password.length() <= Integer.parseInt(env.getProperty("member.password.length.max"));
    }

    private boolean validateUserName(String username) {
        return StringUtils.hasText(username)
                && username.length() <= Integer.parseInt(env.getProperty("member.username.length.max"));
    }

    private boolean validateEmail(String email) {
        return StringUtils.hasText(email)
                && email.length() <= Integer.parseInt(env.getProperty("member.email.length.max"));
    }
}
