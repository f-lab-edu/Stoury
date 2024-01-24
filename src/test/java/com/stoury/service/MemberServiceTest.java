package com.stoury.service;

import com.stoury.domain.Member;
import com.stoury.dto.MemberCreateRequest;
import com.stoury.dto.MemberResponse;
import com.stoury.dto.MemberUpdateRequest;
import com.stoury.exception.MemberCrudExceptions;
import com.stoury.repository.MemberRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Slice;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Stream;

import static com.stoury.service.MemberService.*;
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

    @AfterEach
    void tearDown() {
        memberRepository.deleteAll();
    }
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

    @Test
    @DisplayName("사용자 생성 - 실패, 중복 이메일")
    void createMemberFailBySameEmail() {
        String sameEmail = "alreadt@exists.com";

        Member alreadyExistMember = Member.builder().email(sameEmail)
                .username("member1")
                .encryptedPassword("ewodifh239")
                .build();
        memberRepository.save(alreadyExistMember);

        MemberCreateRequest memberCreateRequest = MemberCreateRequest.builder()
                .email(sameEmail)
                .password("notencryptedpwd")
                .username("member2")
                .build();

        assertThatThrownBy(()-> memberService.createMember(memberCreateRequest))
                .isInstanceOf(MemberCrudExceptions.MemberCreateException.class);
    }

    @ParameterizedTest
    @MethodSource("invalidRequestMembers")
    @DisplayName("사용자 생성 - 실패, [이메일, 패스워드, 이름] 셋 중 하나라도 누락 불가")
    void createMemberFail(MemberCreateRequest defectiveMemberCreateRequest) {
        assertThatThrownBy(() -> memberService.createMember(defectiveMemberCreateRequest))
                .isInstanceOf(MemberCrudExceptions.MemberCreateException.class);
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
                .isInstanceOf(MemberCrudExceptions.MemberDeleteException.class);

        assertThatThrownBy(() -> memberService.deleteMember(null))
                .isInstanceOf(MemberCrudExceptions.MemberDeleteException.class);
    }

    @Test
    @DisplayName("사용자 정보 업데이트 - 성공")
    void updateMemberSuccess() {
        Member member1 = Member.builder().email("mem1@dddd.com").encryptedPassword("vurhf2").username("member1").build();
        Member member2 = Member.builder().email("mem2@dddd.com").encryptedPassword("qwerty").username("member2").build();
        Member savedMember1 = memberRepository.save(member1);
        String imagePathBeforeUpdate = savedMember1.getProfileImagePath();
        memberRepository.save(member2);

        MemberUpdateRequest member1UpdateRequest = com.stoury.dto.MemberUpdateRequest.builder()
                .email(member1.getEmail())
                .username("changed1")
                .introduction("Member1's introduction was changed!")
                .build();
        MultipartFile profileImage = new MockMultipartFile("profileImage1", new byte[0]);
        MemberResponse updatedMember = memberService.updateMember(member1UpdateRequest, profileImage);

        assertThat(updatedMember.email()).isEqualTo(member1UpdateRequest.email());
        assertThat(updatedMember.username()).isEqualTo(member1UpdateRequest.username());
        assertThat(updatedMember.profileImagePath()).isNotEqualTo(imagePathBeforeUpdate);
        assertThat(updatedMember.introduction()).isEqualTo(member1UpdateRequest.introduction());
    }

    @Test
    @DisplayName("사용자 정보 업데이트 - 실패, 사용자 이름이 없음")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void updateMemberFail() {
        Member member1 = Member.builder().email("mem1@dddd.com").encryptedPassword("vurhf2").username("member1").build();
        memberRepository.save(member1);

        MemberUpdateRequest memberUpdateRequest = MemberUpdateRequest.builder()
                .email(member1.getEmail())
                .introduction("I have no name")
                .build();
        MultipartFile profileImage = new MockMultipartFile("profileImage1", new byte[0]);

        assertThatThrownBy(()->memberService.updateMember(memberUpdateRequest, profileImage))
                .isInstanceOf(NullPointerException.class);
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

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "\t", "\r", "\n"})
    @DisplayName("사용자 검색, 실패- 공백 혹은 빈 문자열만 있으면 잘못된 입력값으로 간주")
    void getMembersByBlank(String searchKeyword) {
        assertThatThrownBy(() -> memberService.searchMembers(searchKeyword))
                .isInstanceOf(MemberCrudExceptions.MemberSearchException.class);
    }

    @Test
    @DisplayName("사용자 검색 실패 - null로 검색")
    void getMembersFail() {
        assertThatThrownBy(() -> memberService.searchMembers(null))
                .isInstanceOf(MemberCrudExceptions.MemberSearchException.class);
    }
}