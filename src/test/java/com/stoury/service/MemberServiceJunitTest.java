package com.stoury.service;

import com.stoury.domain.Member;
import com.stoury.dto.member.MemberResponse;
import com.stoury.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Slice;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.stoury.service.MemberService.PAGE_SIZE;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberServiceJunitTest {
    @Autowired
    Environment env;
    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;

    @AfterEach
    void tearDown() {
        memberRepository.deleteAll();
    }

    /*
    네이티브 쿼리를 사용하는 테스트
    테스트 트랜잭션이 적용되면 엔티티가 db로 업데이트 되지 않고
    네이티브 쿼리는 db에 직통으로 동작하기 때문에
    테스트 트랜잭션이 적용되지 않게 해야함.
     */
    @Test
    @DisplayName("사용자 검색")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void getMembers() {
        Member member1 = Member.builder().email("mem1@aaaa.com").encryptedPassword("pwdpwdpwdpwd").username("member1").build();
        Member member2 = Member.builder().email("mem2@aaaa.com").encryptedPassword("pwdpwdpwdpwd").username("member2").build();
        Member member3 = Member.builder().email("mem3@aaaa.com").encryptedPassword("pwdpwdpwdpwd").username("mexber3").build();
        Member member4 = Member.builder().email("mem4@aaaa.com").encryptedPassword("pwdpwdpwdpwd").username("xember4").build();
        Member member5 = Member.builder().email("mem5@aaaa.com").encryptedPassword("pwdpwdpwdpwd").username("member5").build();
        Member member6 = Member.builder().email("mem6@aaaa.com").encryptedPassword("pwdpwdpwdpwd").username("member6").build();
        Member member7 = Member.builder().email("mem7@aaaa.com").encryptedPassword("pwdpwdpwdpwd").username("member7").build();
        Member member8 = Member.builder().email("mem8@aaaa.com").encryptedPassword("pwdpwdpwdpwd").username("member8").build();
        memberRepository.saveAll(List.of(member1, member2, member3, member4, member5, member6, member7, member8));

        Slice<MemberResponse> slice = memberService.searchMembers("mem");
        List<MemberResponse> foundMembers = slice.getContent();

        assertThat(slice).hasSize(PAGE_SIZE);
        assertThat(slice.hasNext()).isTrue();
        assertThat(foundMembers.get(0).username()).isEqualTo(member1.getUsername());
        assertThat(foundMembers.get(1).username()).isEqualTo(member2.getUsername());
        assertThat(foundMembers.get(2).username()).isEqualTo(member5.getUsername());
        assertThat(foundMembers.get(3).username()).isEqualTo(member6.getUsername());
        assertThat(foundMembers.get(4).username()).isEqualTo(member7.getUsername());
    }
}