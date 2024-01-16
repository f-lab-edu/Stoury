package com.stoury.repository;

import com.stoury.domain.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    @Query(value = """
            SELECT *
            FROM MEMBER
            WHERE MATCH(USERNAME) AGAINST (:username'*' IN BOOLEAN MODE)
            """, nativeQuery = true)
    Slice<Member> findAllByUsername(String username, Pageable page);

    List<Member> findAllByDeletedIsTrue();

    boolean existsByEmail(String email);
}
