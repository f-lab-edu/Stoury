package com.stoury.service;

import com.stoury.dto.MemberDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public interface MemberService {
    MemberDto createMember(MemberDto memberDto);
}
