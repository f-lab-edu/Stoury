package com.stoury

import com.stoury.domain.ChatMessage
import com.stoury.domain.ChatRoom
import com.stoury.domain.Member
import com.stoury.repository.ChatMessageRepository
import com.stoury.repository.ChatRoomRepository
import com.stoury.repository.MemberRepositoryJPA
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import java.time.LocalDateTime

@SpringBootTest(classes = ChatStouryApplication.class)
@ActiveProfiles("test")
class ChatIntegrationTest extends Specification {
    @Autowired
    MemberRepositoryJPA memberRepository
    @Autowired
    ChatRoomRepository chatRoomRepository
    @Autowired
    ChatMessageRepository chatMessageRepository

    def member = new Member("aaa@dddd.com", "qwdqwdqwd", "username", null);

    def setup() {
        chatMessageRepository.deleteAll()
        chatRoomRepository.deleteAll()
        memberRepository.deleteAll()
        memberRepository.save(member)
    }

    def cleanup() {
        chatMessageRepository.deleteAll()
        chatRoomRepository.deleteAll()
        memberRepository.deleteAll()
    }

    def "이전 채팅 불러오기"() {
        given:
        def member1 = memberRepository.save(new Member("test1@email.com", "encrypted", "member1", null))
        def member2 = memberRepository.save(new Member("test2@email.com", "encrypted", "member2", null))
        def chatRoom = chatRoomRepository.save(new ChatRoom(member1, member2))
        def firstChat = new ChatMessage(member1, chatRoom, "firstChat", LocalDateTime.of(2024,12,31,13,5))
        def secondChat = new ChatMessage(member2, chatRoom, "secondChat", LocalDateTime.of(2024,12,31,13,10))
        def thirdChat = new ChatMessage(member1, chatRoom, "thirdChat", LocalDateTime.of(2024,12,31,13,15))
        def savedChats = chatMessageRepository.saveAll(List.of(firstChat, secondChat, thirdChat))
        when:
        def prevChats = chatMessageRepository.findAllByChatRoomAndIdLessThan(chatRoom,
                savedChats.get(2).getId(),
                PageRequest.of(0, 10, Sort.by("createdAt").descending()))
        then:
        prevChats.get(0).id == savedChats.get(1).id
        prevChats.get(1).id == savedChats.get(0).id
    }

    def "채팅방 중복 확인 쿼리"() {
        given:
        def member1 = memberRepository.save(new Member("test1@email.com", "encrypted", "member1", null))
        def member2 = memberRepository.save(new Member("test2@email.com", "encrypted", "member2", null))
        chatRoomRepository.save(new ChatRoom(member1, member2))
        expect:
        chatRoomRepository.existsBy([member1, member2] as Set)
    }

    def "existsByMembers([member1, member2])는 member1과 member2를 모두 포함하는 채팅방을 출력한다."() {
        given:
        def member1 = memberRepository.save(new Member("test1@email.com", "encrypted", "member1", null))
        def member2 = memberRepository.save(new Member("test2@email.com", "encrypted", "member2", null))
        def member3 = memberRepository.save(new Member("test3@email.com", "encrypted", "member3", null))
        chatRoomRepository.save(new ChatRoom(member1, member3))
        expect:
        !chatRoomRepository.existsBy([member1, member2] as Set)
    }
}
