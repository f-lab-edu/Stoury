package com.stoury.service;

import com.stoury.domain.Member;
import com.stoury.dto.RequestMember;
import com.stoury.dto.ResponseMember;
import com.stoury.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicMemberService implements MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public ResponseMember createMember(RequestMember requestMember) {
        String encryptedPassword = passwordEncoder.encode(requestMember.password());

        Member member = Member.builder()
                .email(requestMember.email())
                .username(requestMember.username())
                .encryptedPassword(encryptedPassword)
                .introduction(requestMember.introduction())
                .build();

        Member newMember = memberRepository.save(member);

        return ResponseMember.from(newMember);
    }
}
