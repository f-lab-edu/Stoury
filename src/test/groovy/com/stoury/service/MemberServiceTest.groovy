package com.stoury.service

import com.stoury.domain.Member
import com.stoury.dto.member.MemberCreateRequest
import com.stoury.dto.member.MemberUpdateRequest
import com.stoury.exception.member.MemberCreateException
import com.stoury.exception.member.MemberDeleteException
import com.stoury.exception.member.MemberSearchException
import com.stoury.exception.member.MemberUpdateException
import com.stoury.repository.MemberRepository
import com.stoury.service.storage.StorageService
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.crypto.password.PasswordEncoder
import spock.lang.Specification

class MemberServiceTest extends Specification {
    def memberRepository = Mock(MemberRepository)
    def passwordEncoder = Mock(PasswordEncoder)
    def storageService = Mock(StorageService)

    def memberService = new MemberService(memberRepository, passwordEncoder, storageService)

    def "사용자 생성 - 성공"() {
        given:
        def memberCreateRequest = MemberCreateRequest.builder()
                .email("dddd@cccc.com")
                .password("notencryptedpwd")
                .username("jzakka")
                .build()

        when:
        memberService.createMember(memberCreateRequest)

        then:
        1 * memberRepository.save(_ as Member) >> Stub(Member)
    }

    def "사용자 생성 - 실패, 중복 이메일"() {
        given:
        memberRepository.existsByEmail(_ as String) >> true
        MemberCreateRequest memberCreateRequest = MemberCreateRequest.builder()
                .email("already@Exists.com")
                .password("notencryptedpwd")
                .username("member2")
                .build();

        when:
        memberService.createMember(memberCreateRequest)
        then:
        thrown(MemberCreateException.class)
    }

    def "사용자 생성 - 실패, [이메일, 패스워드, 이름] 셋 중 하나라도 누락 불가"() {
        given:
        def request = new MemberCreateRequest(email, password, username, "message")

        when:
        memberService.createMember(request)

        then:
        thrown(MemberCreateException.class)

        where:
        email             | password         | username
        null              | "pppppwwwwwaaaa" | "chung"    // 이메일 누락
        "qwdqws@qqqq.com" | null             | "sang"    // 패스워드 누락
        "qwdqws@qqqq.com" | "pppppwwwwwaaaa" | null     // 이름 누락
    }

    def "사용자 삭제 - 성공"() {
        given:
        def member = Mock(Member)
        memberRepository.findById(_) >> Optional.of(member)
        when:
        memberService.deleteMember(1L)
        then:
        1 * member.delete()
    }

    def "사용자 삭제 - 실패, 존재하지 않는 사용자"() {
        given:
        memberRepository.findById(_) >> Optional.empty()
        when:
        memberService.deleteMember(1L)
        then:
        thrown(MemberDeleteException.class)
    }

    def "프사없이 사용자 정보 업데이트 - 성공"() {
        given:
        def member = Mock(Member)
        memberRepository.findById(_) >> Optional.of(member)
        when:
        memberService.updateMember(new MemberUpdateRequest(1L, "email", "username", null))
        then:
        1 * member.update(_,_,_)
    }

    def "프사랑 사용자 정보 업데이트 - 성공"() {
        given:
        def member = Mock(Member)
        memberRepository.findById(_) >> Optional.of(member)
        def memberUpdateRequest = new MemberUpdateRequest(1L, "email", "username", null)
        def profileImage = new MockMultipartFile("Files", "profileImage",
                "image/jpeg", new byte[0]);
        when:
        memberService.updateMemberWithProfileImage(memberUpdateRequest, profileImage)
        then:
        1 * storageService.saveFileAtPath(_, _)
        1 * member.update(_,_,_)
    }

    def "프사랑 사용자 정보 업데이트 - 실패, jpeg가 아님"() {
        given:
        def request = MemberUpdateRequest.builder().build()
        def profileImage = new MockMultipartFile("Files", "profileImage",
                "image/png", new byte[0]);
        when:
        memberService.updateMemberWithProfileImage(request, profileImage)
        then:
        thrown(MemberUpdateException.class)
    }

    def "사용자 정보 업데이트 - 실패, 사용자 이름이 없음"() {
        given:
        memberRepository.findByEmail(_) >> Optional.of(new Member())
        def memberUpdateRequest = MemberUpdateRequest.builder()
                .email("asdasd@asdasd.com")
                .introduction("I have no name")
                .build();
        when:
        memberService.updateMember(memberUpdateRequest)
        then:
        thrown(NullPointerException.class)
    }

    def "사용자 검색, 실패- 공백 혹은 빈 문자열만 있으면 잘못된 입력값으로 간주"() {
        when:
        memberService.searchMembers(keyword)
        then:
        thrown(MemberSearchException)
        where:
        keyword << ["", " ", "\t", "\r", "\n", null]
    }
}
