package com.stoury.service;

import com.stoury.domain.Member;
import com.stoury.dto.MemberCreateRequest;
import com.stoury.dto.MemberResponse;
import com.stoury.dto.MemberUpdateRequest;
import com.stoury.exception.MemberCreateException;
import com.stoury.exception.MemberDeleteException;
import com.stoury.exception.MemberSearchException;
import com.stoury.exception.MemberUpdateException;
import com.stoury.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final static int PASSWORD_LENGTH_MIN = 8;
    private final static int PASSWORD_LENGTH_MAX = 30;
    private final static int USERNAME_LENGTH_MAX = 10;
    private final static int EMAIL_LENGTH_MAX = 25;
    private final static int PAGE_SIZE = 5;

    @Transactional
    public MemberResponse createMember(MemberCreateRequest memberCreateRequest) {
        validateRequestMember(memberCreateRequest);
        String encryptedPassword = passwordEncoder.encode(memberCreateRequest.password());

        Member member = memberCreateRequest.toEntity(encryptedPassword);

        Member newMember = memberRepository.save(member);

        return MemberResponse.from(newMember);
    }

    private void validateRequestMember(MemberCreateRequest memberCreateRequest) {
        String email = memberCreateRequest.email();
        String username = memberCreateRequest.username();
        String password = memberCreateRequest.password();

        if (validateEmail(email) && validateUserName(username) && validatePassword(password)) {
            return;
        }
        throw new MemberCreateException();
    }

    private boolean validatePassword(String password) {
        return StringUtils.hasText(password)
                && password.length() >= PASSWORD_LENGTH_MIN
                && password.length() <= PASSWORD_LENGTH_MAX;
    }

    private boolean validateUserName(String username) {
        return StringUtils.hasText(username)
                && username.length() <= USERNAME_LENGTH_MAX;
    }

    private boolean validateEmail(String email) {
        return StringUtils.hasText(email)
                && email.length() <= EMAIL_LENGTH_MAX
                && !memberRepository.existsByEmail(email);
    }

    @Transactional
    public void deleteMember(Long memberId) {
        if (memberId == null) {
            throw new MemberDeleteException();
        }

        Member deleteMember = memberRepository
                .findById(memberId)
                 .orElseThrow(MemberDeleteException::new);

        deleteMember.delete();
    }

    @Transactional
    public MemberResponse updateMember(MemberUpdateRequest memberUpdateRequest) {
        Member updateMember = findByIdOrEmail(memberUpdateRequest);

        updateMember.update(
                Objects.requireNonNull(memberUpdateRequest.username()),
                memberUpdateRequest.profileImagePath(),
                memberUpdateRequest.introduction()
        );

        return MemberResponse.from(updateMember);
    }

    private Member findByIdOrEmail(MemberUpdateRequest memberUpdateRequest) {
        Member updateMember;
        if (memberUpdateRequest.id() == null) {
            updateMember = memberRepository
                    .findByEmail(memberUpdateRequest.email())
                    .orElseThrow(MemberUpdateException::new);
        } else {
            updateMember = memberRepository
                    .findById(memberUpdateRequest.id())
                    .orElseThrow(MemberUpdateException::new);
        }
        return updateMember;
    }

    @Transactional(readOnly = true)
    public List<MemberResponse> searchMembers(String username) {
        if (username == null) {
            throw new MemberSearchException();
        }

        username = username.replaceAll("\\s+", "");

        Pageable page = PageRequest.of(0, PAGE_SIZE, Sort.by("username"));

        // TODO: like %%말고 다른 방법 강구
        List<Member> foundMembers = memberRepository.findAllByUsernameLikeIgnoreCase("%"+username+"%", page);

        return foundMembers.stream().map(MemberResponse::from).toList();
    }
}
