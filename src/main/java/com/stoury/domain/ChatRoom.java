package com.stoury.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "CHAT_ROOM")
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinTable(name = "CHAT_ROOM_MEMBER",
            joinColumns = @JoinColumn(name = "CHAT_ROOM_ID"),
            inverseJoinColumns = @JoinColumn(name = "MEMBER_ID", nullable = true))
    @ManyToMany
    List<Member> members = new ArrayList<>();

    public ChatRoom(List<Member> members) {
        if (members.size() != 2 || members.get(0).equals(members.get(1))) {
            throw new IllegalArgumentException("Only one-on-one chat is available." );
        }
        this.members = members;
    }

    public ChatRoom(Member member1, Member member2) {
        this.members = List.of(member1, member2);
    }

    public boolean hasMember(Member member) {
        return members.contains(member);
    }
}
