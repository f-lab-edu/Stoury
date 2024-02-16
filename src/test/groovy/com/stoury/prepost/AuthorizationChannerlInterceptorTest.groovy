package com.stoury.prepost

import com.stoury.config.websocket.AuthorizationChannelInterceptor
import com.stoury.service.ChatService
import com.stoury.utils.JwtUtils
import spock.lang.Specification

class AuthorizationChannerlInterceptorTest extends Specification {
    def jwtUtils = new JwtUtils()
    def chatService = Mock(ChatService)
    def interceptor = new AuthorizationChannelInterceptor(jwtUtils, chatService)

    def setup() {
        jwtUtils.tokenSecret = "tokensecret"
    }

    def "채팅방 멤버만 채팅방 채널 구독 가능"() {
        given:
        def roomId = 1
        def memberId = 1
        and:
        def token = jwtUtils.issueToken(memberId, roomId)
        when:
        interceptor.checkRoomMember(token)
        then:
        1 * chatService.checkIfRoomMember(1,1)
    }
}
