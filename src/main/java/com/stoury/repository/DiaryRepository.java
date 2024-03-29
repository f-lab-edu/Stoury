package com.stoury.repository;

import com.stoury.domain.Diary;
import com.stoury.domain.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    @Query("""
    SELECT D
    FROM Diary D JOIN FETCH D.thumbnail
    WHERE D.member = :member AND D.id < :offsetId
    """
    )
    List<Diary> findByMemberAndIdLessThan(Member member, Long offsetId, Pageable page);
}
