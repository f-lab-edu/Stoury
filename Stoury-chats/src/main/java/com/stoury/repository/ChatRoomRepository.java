package com.stoury.repository;

import com.stoury.domain.ChatRoom;
import com.stoury.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    @Query("""
    SELECT COUNT (CR) > 0
    FROM ChatRoom CR JOIN CR.members M
    WHERE M IN :members
    """)
    boolean existsByMembers(List<Member> members);
}
