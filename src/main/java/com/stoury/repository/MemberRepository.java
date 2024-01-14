package com.stoury.repository;

import com.stoury.domain.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    List<Member> findAllByUsernameLikeIgnoreCase(String username, Pageable page);

    List<Member> findAllByDeletedIsTrue();
}
