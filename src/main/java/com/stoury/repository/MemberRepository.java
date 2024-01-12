package com.stoury.repository;

import com.stoury.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    @Modifying
    @Query("update Member m set m.deleted = true where m.email = :email")
    int deleteByEmail(String email);

    List<Member> findAllByDeleted(boolean deleted);
}
