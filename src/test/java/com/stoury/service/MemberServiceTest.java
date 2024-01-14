package com.stoury.service;

import com.stoury.dto.MemberCreateRequest;
import com.stoury.dto.MemberResponse;
import com.stoury.domain.Member;
import com.stoury.dto.MemberUpdateRequest;
import com.stoury.exception.MemberCreateException;
import com.stoury.exception.MemberDeleteException;
import com.stoury.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MemberServiceTest {
    @Autowired
    Environment env;
    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
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

    @Test
    @DisplayName("사용자 삭제 - 성공")
    void deleteMemberSuccess() {
        Member member1 = Member.builder().email("mem1@dddd.com").encryptedPassword("vurhf2").username("member1").build();
        Member member2 = Member.builder().email("mem2@dddd.com").encryptedPassword("qwerty").username("member2").build();
        Long member1Id = memberRepository.save(member1).getId();
        Long member2Id = memberRepository.save(member2).getId();

        memberService.deleteMember(member1Id);

        List<Long> deleteMembersIds = memberRepository.findAllByDeletedIsTrue().stream()
                .map(Member::getId)
                .toList();

        assertThat(deleteMembersIds).hasSize(1);
        assertThat(deleteMembersIds).containsExactly(member1Id);
    }

    @Test
    @DisplayName("사용자 삭제 - 실패, 존재하지 않는 사용자")
    void deleteMemberFail() {
        Member member1 = Member.builder().email("mem1@dddd.com").encryptedPassword("vurhf2").username("member1").build();
        Member member2 = Member.builder().email("mem2@dddd.com").encryptedPassword("qwerty").username("member2").build();
        memberRepository.save(member1);
        memberRepository.save(member2);

        Long notExistingId = member1.getId() + member2.getId();

        assertThatThrownBy(()->memberService.deleteMember(notExistingId))
                .isInstanceOf(MemberDeleteException.class);

        assertThatThrownBy(() -> memberService.deleteMember(null))
                .isInstanceOf(MemberDeleteException.class);
    }

    @Test
    @DisplayName("사용자 정보 업데이트 - 성공")
    void updateMemberSuccess() {
        Member member1 = Member.builder().email("mem1@dddd.com").encryptedPassword("vurhf2").username("member1").build();
        Member member2 = Member.builder().email("mem2@dddd.com").encryptedPassword("qwerty").username("member2").build();
        memberRepository.save(member1);
        memberRepository.save(member2);

        MemberUpdateRequest MemberUpdateRequest = com.stoury.dto.MemberUpdateRequest.builder()
                .email(member1.getEmail())
                .username("changed1")
                .profileImagePath("/profile/images/member1")
                .introduction("Member1's introduction was changed!")
                .build();
        MemberResponse updatedMember = memberService.updateMember(MemberUpdateRequest);

        assertThat(updatedMember.email()).isEqualTo(MemberUpdateRequest.email());
        assertThat(updatedMember.username()).isEqualTo(MemberUpdateRequest.username());
        assertThat(updatedMember.profileImagePath()).isEqualTo(MemberUpdateRequest.profileImagePath());
        assertThat(updatedMember.introduction()).isEqualTo(MemberUpdateRequest.introduction());
    }

    @Test
    @DisplayName("사용자 정보 업데이트 - 실패, 사용자 이름이 없음")
    void updateMemberFail() {
        Member member1 = Member.builder().email("mem1@dddd.com").encryptedPassword("vurhf2").username("member1").build();
        memberRepository.save(member1);

        MemberUpdateRequest memberUpdateRequest = MemberUpdateRequest.builder()
                .email(member1.getEmail())
                .profileImagePath("/profile/images/member1")
                .introduction("I have no name")
                .build();

        assertThatThrownBy(()->memberService.updateMember(memberUpdateRequest))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("사용자 검색")
    void getMembers() {
        Member member1 = Member.builder().email("mem1@aaaa.com").encryptedPassword("pwdpwdpwdpwd").username("member1").build();
        Member member2 = Member.builder().email("mem2@aaaa.com").encryptedPassword("pwdpwdpwdpwd").username("member2").build();
        Member member3 = Member.builder().email("mem3@aaaa.com").encryptedPassword("pwdpwdpwdpwd").username("menber3").build();
        Member member4 = Member.builder().email("mem4@aaaa.com").encryptedPassword("pwdpwdpwdpwd").username("nember4").build();
        memberRepository.saveAll(List.of(member1, member2, member3, member4));

        List<MemberResponse> foundMembers = memberService.getMembers("mem");

        assertThat(foundMembers).hasSize(2);
        assertThat(foundMembers.get(0).username()).isEqualTo(member1.getUsername());
        assertThat(foundMembers.get(1).username()).isEqualTo(member2.getUsername());
    }

    @Test
    @DisplayName("사용자 검색 실패 - null로 검색")
    void getMembersFail() {
        assertThatThrownBy(() -> memberService.getMembers(null)).isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t", "\r", "\n"})
    @DisplayName("사용자 검색 - 공백 혹은 빈 문자열은 페이지 크기만큼 사용자 가져옴")
    void getMembersByBlank(String searchKeyword) {
        prepareMembers(20);

        int pageSize = Integer.parseInt(env.getProperty("member.pagesize"));

        List<MemberResponse> foundByEmptyString = memberService.getMembers(searchKeyword);
        assertThat(foundByEmptyString).hasSize(pageSize);
    }

    private void prepareMembers(int totalMembers) {
        for (int i = 0; i < totalMembers; i++) {
            Member member = Member.builder()
                    .email("mem"+i+"@aaaa.com")
                    .encryptedPassword("pwdpwdpwdpwd")
                    .username("member" + i)
                    .build();
            memberRepository.save(member);
        }
    }
}