package com.stoury.service;

import com.stoury.dto.RequestMember;
import com.stoury.dto.ResponseMember;
import com.stoury.exception.MemberCreateException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
class MemberServiceTest {
    @Autowired
    MemberService memberService;

    @Test
    @DisplayName("사용자 생성 - 성공")
    void createMemberSuccess() {
        RequestMember requestMember = RequestMember.builder()
                .email("dddd@cccc.com")
                .password("notencryptedpwd")
                .username("jzakka")
                .build();

        ResponseMember createMember = memberService.createMember(requestMember);

        assertThat(createMember.email()).isEqualTo(requestMember.email());
        assertThat(createMember.username()).isEqualTo(requestMember.username());
    }

    @ParameterizedTest
    @MethodSource("invalidRequestMembers")
    @DisplayName("사용자 생성 - 실패, [이메일, 패스워드, 이름] 셋 중 하나라도 누락 불가")
    void createMemberFail(RequestMember requestMember) {
        assertFailByException(requestMember);
    }

    private static Stream<Arguments> invalidRequestMembers() {
        return Stream.of(
                Arguments.of(RequestMember.builder().password("pppppwwwwwaaaa").username("chung").build()),
                Arguments.of(RequestMember.builder().email("qwdqws@qqqq.com").username("sang").build()),
                Arguments.of(RequestMember.builder().email("qwdqws@qqqq.com").password("pppppwwwwwaaaa").build())
        );
    }

    private void assertFailByException(RequestMember noUsername) {
        assertThatThrownBy(() -> memberService.createMember(noUsername))
                .isInstanceOf(MemberCreateException.class);
    }
}