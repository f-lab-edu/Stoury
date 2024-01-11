package com.stoury.service;

import com.stoury.dto.MemberDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
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
        assertThat(createMember.getPassword()).isNotEqualTo(memberDto.getPassword());
        assertThat(createMember.getPassword()).hasSize(60);
    }
}