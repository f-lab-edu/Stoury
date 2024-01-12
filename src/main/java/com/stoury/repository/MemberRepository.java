package com.stoury.repository;

import com.stoury.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByEmail(String email);

    List<Member> findAllByDeleted(boolean deleted);

    List<Member> findAllByUsernameLikeIgnoreCaseOrderByUsername(String username);
}
