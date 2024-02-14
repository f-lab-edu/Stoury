package com.stoury.service

import com.stoury.domain.ChatRoom
import com.stoury.domain.Member
import com.stoury.repository.ChatMessageRepository
import com.stoury.repository.ChatRoomRepository
import com.stoury.repository.MemberRepository
import spock.lang.Specification

class ChatServiceTest extends Specification {
    def memerRepository = Mock(MemberRepository)
    def chatRoomRepository = Mock(ChatRoomRepository)
    def chatMessageRepository = Mock(ChatMessageRepository)
    def chatService = new ChatService(memerRepository, chatRoomRepository, chatMessageRepository)

    def "채팅방 개설"() {
        given:
        def sender = new Member("sender@email.com", "pwdpwd123", "sender", null)
        def receiver = new Member("receiver@email.com", "pwdpwd123", "receiver", null)
        sender.id = 1L;
        receiver.id = 2L;
        memerRepository.findById(sender.id) >> Optional.of(sender)
        memerRepository.findById(receiver.id) >> Optional.of(receiver)

        when:
        def chatRoomResponse = chatService.createChatRoom(sender.id, receiver.id)

        then:
        1 * chatRoomRepository.save(_ as ChatRoom) >> new ChatRoom(List.of(sender, receiver))
        chatRoomResponse.members().size() == 2
    }
}
