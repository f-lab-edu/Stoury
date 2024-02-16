package com.stoury.prepost

import com.stoury.config.AuthorizationHeaderFilter
import com.stoury.utils.JwtUtils
import spock.lang.Specification

class AuthorizationHeaderFilterTest extends Specification {
    def jwtUtils = new JwtUtils()
    def authHeaderFilter = new AuthorizationHeaderFilter(jwtUtils)

    def "채팅방 id 추출 확인"() {
        given:
        def servletPath = "/chatRoom/auth/123892?blablabla=213123"
        expect:
        authHeaderFilter.getChatRoomId(servletPath) == 123892
    }
}
