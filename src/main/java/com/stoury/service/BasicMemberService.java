package com.stoury.service;

import com.stoury.domain.Member;
import com.stoury.dto.MemberDto;
import com.stoury.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BasicMemberService implements MemberService {
    private final MemberRepository memberRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    @Override
    public MemberDto createMember(MemberDto memberDto) {
        Member member = modelMapper.map(memberDto, Member.class);

        String encryptedPassword = passwordEncoder.encode(memberDto.getPassword());
        member.setEncryptedPassword(encryptedPassword);

        memberRepository.save(member);

        return modelMapper.map(member, MemberDto.class);
    }
}
