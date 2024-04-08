package com.stoury.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

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
    Set<Member> members = new HashSet<>();

    public ChatRoom(Set<Member> members) {
        if (members.size() != 2) {
            throw new IllegalArgumentException("Only one-on-one chat is available." );
        }
        this.members = members;
    }

    public ChatRoom(Member member1, Member member2) {
        this.members = Set.of(member1, member2);
    }

    public boolean hasMember(Member member) {
        return members.contains(member);
    }
}
