package com.stoury.service;

import com.stoury.dto.RequestMember;
import com.stoury.dto.ResponseMember;
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

    RequestMember requestMember;

    @BeforeEach
    void setup() {
        requestMember = new RequestMember(
                "dddd@cccc.com",
                "notencryptedpwd",
                "jzakka",
                null);
    }

    @Test
    @DisplayName("사용자 생성 - 성공")
    void createMemberSuccess() {
        ResponseMember createMember = memberService.createMember(requestMember);

        assertThat(createMember.email()).isEqualTo(requestMember.email());
        assertThat(createMember.username()).isEqualTo(requestMember.username());
    }

    @Test
    @DisplayName("사용자 생성 - 실패, [이메일, 패스워드, 이름] 셋 중 하나라도 누락 불가")
    void createMemberFail() {
        RequestMember noEmail = new RequestMember(null,"pppppwwwwwaaaa","chung", null);
        assertFailByException(noEmail);

        RequestMember noPassword = new RequestMember("qwdqws@qqqq.com", null, "sang", null);
        assertFailByException(noPassword);

        RequestMember noUsername = new RequestMember("qwdqws@qqqq.com", "pppppwwwwwaaaa", null, null);
        assertFailByException(noUsername);
    }

    private void assertFailByException(RequestMember noUsername) {
        assertThatThrownBy(() -> memberService.createMember(noUsername))
                .isInstanceOf(MemberCreateException.class);
    }
}