package com.stoury.service;

import com.stoury.dto.MemberCreateRequest;
import com.stoury.dto.MemberResponse;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface MemberService {
    MemberResponse createMember(MemberCreateRequest memberDto);
}
