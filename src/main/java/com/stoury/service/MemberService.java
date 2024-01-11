package com.stoury.service;

import com.stoury.dto.MemberDto;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface MemberService {
    MemberDto createMember(MemberDto memberDto);
}
