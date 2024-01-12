package com.stoury.service;

import com.stoury.dto.RequestMember;
import com.stoury.dto.ResponseMember;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface MemberService {
    ResponseMember createMember(RequestMember memberDto);
}
