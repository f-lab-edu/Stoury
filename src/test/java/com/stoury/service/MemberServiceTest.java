package com.stoury.service;

import com.stoury.dto.MemberDto;
import com.stoury.exception.MemberCreateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class MemberServiceTest {
    @Autowired
    MemberService memberService;

    MemberDto memberDto;

    @BeforeEach
    void setup() {
        memberDto = MemberDto.builder()
                .email("dddd@cccc.com")
                .username("jzakka")
                .password("notencryptedpwd")
                .build();
    }

    @Test
    @DisplayName("사용자 생성 - 성공")
    void createMemberSuccess() {
        MemberDto createMember = memberService.createMember(memberDto);

        assertThat(createMember.getEmail()).isEqualTo(memberDto.getEmail());
        assertThat(createMember.getUsername()).isEqualTo(memberDto.getUsername());
    }

    @Test
    @DisplayName("사용자 생성 - 실패, [이메일, 패스워드, 이름] 셋 중 하나라도 누락 불가")
    void createMemberFail() {
        MemberDto noEmail = MemberDto.builder().password("pppppwwwwwaaaa").username("chung").build();
        assertFailByException(noEmail);

        MemberDto noPassword = MemberDto.builder().email("qwdqws@qqqq.com").username("sang").build();
        assertFailByException(noPassword);

        MemberDto noUsername = MemberDto.builder().email("qwdqws@qqqq.com").password("pppppwwwwwaaaa").build();
        assertFailByException(noUsername);
    }

    private void assertFailByException(MemberDto noUsername) {
        assertThatThrownBy(() -> memberService.createMember(noUsername))
                .isInstanceOf(MemberCreateException.class);
    }
}