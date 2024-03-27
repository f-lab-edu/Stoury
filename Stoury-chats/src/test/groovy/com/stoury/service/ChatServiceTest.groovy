package com.stoury.service

import com.stoury.config.sse.SseEmitters
import com.stoury.domain.ChatMessage
import com.stoury.domain.ChatRoom
import com.stoury.domain.Member
import com.stoury.dto.chat.ChatMessageResponse
import com.stoury.event.ChatMessageSaveEvent
import com.stoury.exception.authentication.NotAuthorizedException
import com.stoury.exception.chat.ChatRoomCreateException
import com.stoury.repository.ChatMessageRepository
import com.stoury.repository.ChatRoomRepository
import com.stoury.repository.MemberRepository
import com.stoury.service.kafka.KafkaProducer
import org.springframework.context.ApplicationEventPublisher
import spock.lang.Specification

import java.time.LocalDateTime

class ChatServiceTest extends Specification {
    def memerRepository = Mock(MemberRepository)
    def chatRoomRepository = Mock(ChatRoomRepository)
    def chatMessageRepository = Mock(ChatMessageRepository)
    def sseEmitters = Mock(SseEmitters)
    def eventPublisher = Mock(ApplicationEventPublisher)
    def kafkaProducer = Mock(KafkaProducer)
    def chatService = new ChatService(memerRepository, chatRoomRepository, chatMessageRepository, sseEmitters, eventPublisher,kafkaProducer)

    def "채팅방 개설"() {
        given:
        def sender = new Member("sender@email.com", "pwdpwd123", "sender", null)
        def receiver = new Member("receiver@email.com", "pwdpwd123", "receiver", null)
        sender.id = 1L;
        receiver.id = 2L;
        memerRepository.findAllById([1,2]) >> [sender, receiver]

        when:
        def chatRoomResponse = chatService.createChatRoom(sender.id, receiver.id)

        then:
        1 * chatRoomRepository.save(_ as ChatRoom) >> new ChatRoom([sender, receiver] as Set)
        chatRoomResponse.members().size() == 2
    }

    def "중복 채팅방 개설-실패"() {
        given:
        def sender = new Member("sender@email.com", "pwdpwd123", "sender", null)
        def receiver = new Member("receiver@email.com", "pwdpwd123", "receiver", null)
        sender.id = 1L;
        receiver.id = 2L;
        memerRepository.findAllById([1,2]) >> [sender, receiver]
        chatRoomRepository.existsBy([sender, receiver] as Set) >> true
        when:
        chatService.createChatRoom(sender.id, receiver.id)

        then:
        thrown(ChatRoomCreateException)
    }

    def "채팅메시지 생성"() {
        given:
        def sender = new Member("sender@email.com", "pwdpwd123", "sender", null)
        def chatRoom = new ChatRoom([sender, Mock(Member)] as Set)
        sender.id = 1
        chatRoom.id = 1
        memerRepository.findById(sender.id) >> Optional.of(sender)
        chatRoomRepository.findById(chatRoom.id) >> Optional.of(chatRoom)
        when:
        chatService.publishChatMessageSaveEvent(sender.id, chatRoom.id, "Hello, World!")
        then:
        1 * eventPublisher.publishEvent(_ as ChatMessageSaveEvent)
    }

    def "이전 채팅 불러오기"() {
        given:
        def sender1 = new Member("sender1@email.com", "pwdpwd123", "sender1", null)
        def sender2 = new Member("sender2@email.com", "pwdpwd123", "sender2", null)
        def chatRoom = new ChatRoom([sender1, sender2] as Set)
        sender1.id = 1
        sender2.id = 2
        chatRoom.id = 1
        def chatLogs = List.of(
                new ChatMessage(sender1, chatRoom, "Hi, sender2! How are you?", LocalDateTime.now()),
                new ChatMessage(sender2, chatRoom, "Sorry, I dont speak english.", LocalDateTime.now()),
                new ChatMessage(sender1, chatRoom, "Oh, where are you from?", LocalDateTime.now()),
                new ChatMessage(sender2, chatRoom, "I said i dont speak english. I'm korean.", LocalDateTime.now()),
                new ChatMessage(sender1, chatRoom, "Haha, ur lying.", LocalDateTime.now())
        )
        memerRepository.findById(sender1.id) >> Optional.of(sender1)
        chatRoomRepository.findById(chatRoom.id) >> Optional.of(chatRoom)
        when:
        def messages = chatService.getPreviousChatMessages(sender1.id, chatRoom.id, null)
        then:
        1 * chatMessageRepository.findAllByChatRoomAndCreatedAtBefore(_, _, _) >> chatLogs
        messages.size() == 5
    }

    def "이전 채팅 조회 접근 제한"() {
        given:
        def sender1 = new Member("sender1@email.com", "pwdpwd123", "sender1", null)
        def chatRoom = new ChatRoom()
        sender1.id = 1
        chatRoom.id = 1
        memerRepository.findById(sender1.id) >> Optional.of(sender1)
        chatRoomRepository.findById(chatRoom.id) >> Optional.of(chatRoom)
        when:
        chatService.getPreviousChatMessages(sender1.id, chatRoom.id, null)
        then:
        thrown(NotAuthorizedException)
    }

    def "탈퇴한 사용자의 채팅은 (알수없음)으로 표시"() {
        given:
        def sender1 = new Member("sender1@email.com", "pwdpwd123", "sender1", null)
        def sender2 = new Member("sender2@email.com", "pwdpwd123", "sender2", null)
        def chatRoom = new ChatRoom([sender1, sender2] as Set)
        sender1.id = 1
        sender2.id = 2
        sender2.deleted = true
        chatRoom.id = 1
        def chatLogs = List.of(
                new ChatMessage(sender1, chatRoom, "Hi, sender2! How are you?", LocalDateTime.now()),
                new ChatMessage(sender2, chatRoom, "Sorry, I dont speak english.", LocalDateTime.now()),
                new ChatMessage(sender1, chatRoom, "Oh, where are you from?", LocalDateTime.now()),
                new ChatMessage(sender2, chatRoom, "I said i dont speak english. I'm korean.", LocalDateTime.now()),
                new ChatMessage(sender1, chatRoom, "Haha, ur lying.", LocalDateTime.now())
        )
        memerRepository.findById(sender1.id) >> Optional.of(sender1)
        chatRoomRepository.findById(chatRoom.id) >> Optional.of(chatRoom)
        when:
        def messages = chatService.getPreviousChatMessages(sender1.id, chatRoom.id, null)
        then:
        1 * chatMessageRepository.findAllByChatRoomAndCreatedAtBefore(_, _, _) >> chatLogs
        messages.get(1).sender().username() == "UNKNOWN"
        messages.get(3).sender().username() == "UNKNOWN"
    }

    def "채팅 전송"() {
        given:
        Member member = new Member(id: 1)
        ChatRoom chatRoom = Spy(new ChatRoom(id: 2))
        chatRoom.hasMember(member) >> true

        memerRepository.findById(1) >> Optional.of(member)
        chatRoomRepository.findById(2) >> Optional.of(chatRoom)
        chatMessageRepository.save(_) >> new ChatMessage(id:3, sender: member, chatRoom: chatRoom, textContent: "Hi")
        when:
        chatService.sendChatMessage(1, 2, "Hi!")
        then:
        1 * kafkaProducer.produce(_ as ChatMessageResponse)
    }

    def "채팅전송 불가, 내가 참여한 채팅방이 아님"() {
        given:
        Member member = new Member(id: 1)
        ChatRoom chatRoom = Spy(new ChatRoom(id: 2))
        memerRepository.findById(1) >> Optional.of(member)
        chatRoomRepository.findById(2) >> Optional.of(chatRoom)
        when:
        chatService.sendChatMessage(1, 2, "Hi!")
        then:
        thrown(NotAuthorizedException)
    }

    def "채팅전송 불가, 메시지 없음"() {
        given:
        def sender = new Member("sender@email.com", "pwdpwd123", "sender", null)
        def chatRoom = new ChatRoom([sender, Mock(Member)] as Set)
        sender.id = 1
        chatRoom.id = 1
        memerRepository.findById(sender.id) >> Optional.of(sender)
        chatRoomRepository.findById(chatRoom.id) >> Optional.of(chatRoom)
        when:
        chatService.sendChatMessage(sender.id, chatRoom.id, "")
        then:
        thrown(IllegalArgumentException)
    }
}
