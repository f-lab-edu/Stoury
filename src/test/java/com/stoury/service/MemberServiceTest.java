package com.stoury.service;

import com.stoury.dto.MemberCreateRequest;
import com.stoury.dto.MemberResponse;
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
        MemberCreateRequest memberCreateRequest = MemberCreateRequest.builder()
                .email("dddd@cccc.com")
                .password("notencryptedpwd")
                .username("jzakka")
                .build();

        MemberResponse createMember = memberService.createMember(memberCreateRequest);

        assertThat(createMember.email()).isEqualTo(memberCreateRequest.email());
        assertThat(createMember.username()).isEqualTo(memberCreateRequest.username());
    }

    @ParameterizedTest
    @MethodSource("invalidRequestMembers")
    @DisplayName("사용자 생성 - 실패, [이메일, 패스워드, 이름] 셋 중 하나라도 누락 불가")
    void createMemberFail(MemberCreateRequest defectiveMemberCreateRequest) {
        assertThatThrownBy(() -> memberService.createMember(defectiveMemberCreateRequest))
                .isInstanceOf(MemberCreateException.class);
    }

    private static Stream<Arguments> invalidRequestMembers() {
        MemberCreateRequest noEmail = new MemberCreateRequest(null, "pppppwwwwwaaaa",
                "chung", "I have no email");
        MemberCreateRequest noPassword = new MemberCreateRequest("qwdqws@qqqq.com", null,
                "sang", "I have no password");
        MemberCreateRequest noUsername = new MemberCreateRequest("qwdqws@qqqq.com", "pppppwwwwwaaaa",
                null, "I have no username");

        return Stream.of(
                Arguments.of(noEmail),
                Arguments.of(noPassword),
                Arguments.of(noUsername)
        );
    }
}