package com.stoury.repository;

import com.stoury.domain.ChatRoom;
import com.stoury.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.Set;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    default boolean existsBy(Set<Member> members){
        return findByMembers(members, members.size()).isPresent();
    }

    @Query("""
            SELECT c
            FROM ChatRoom c INNER JOIN c.members M
            WHERE M IN :members
            GROUP BY c.id
            HAVING COUNT(c) = :memberCount""")
    Optional<ChatRoom> findByMembers(Set<Member> members, int memberCount);
}
