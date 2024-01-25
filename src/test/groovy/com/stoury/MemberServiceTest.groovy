package com.stoury

import com.stoury.domain.Member
import com.stoury.dto.MemberCreateRequest
import com.stoury.repository.MemberRepository
import com.stoury.service.MemberService
import com.stoury.service.StorageService
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
}
