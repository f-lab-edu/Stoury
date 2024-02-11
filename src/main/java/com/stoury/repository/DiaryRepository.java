package com.stoury.repository;

import com.stoury.domain.Diary;
import com.stoury.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryRepository extends JpaRepository<Diary, Long> {
    Page<Diary> findByMember(Member member, Pageable page);
}
