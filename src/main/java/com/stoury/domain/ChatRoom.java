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
        this.members = members;
    }
}
