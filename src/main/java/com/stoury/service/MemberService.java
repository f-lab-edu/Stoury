package com.stoury.service;

import com.stoury.domain.Member;
import com.stoury.dto.MemberCreateRequest;
import com.stoury.dto.MemberResponse;
import com.stoury.dto.MemberUpdateRequest;
import com.stoury.repository.MemberRepository;
import com.stoury.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.util.Pair;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

import static com.stoury.exception.MemberCrudExceptions.*;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final StorageService storageService;
    public final static int PASSWORD_LENGTH_MIN = 8;
    public final static int PASSWORD_LENGTH_MAX = 30;
    public final static int USERNAME_LENGTH_MAX = 10;
    public final static int EMAIL_LENGTH_MAX = 25;
    public final static int PAGE_SIZE = 5;
    public final static String PROFILE_IMAGE_PATH_PREFIX = "/members/profiles/images";

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

        validateEmail(email);
        validateUserName(username);
        validatePassword(password);
    }

    private void validatePassword(String password) {
        if (StringUtils.hasText(password) && password.length() >= PASSWORD_LENGTH_MIN
                && password.length() <= PASSWORD_LENGTH_MAX) {
            return;
        }
        throw new MemberCreateException("Invalid Password!");
    }

    private void validateUserName(String username) {
        if (StringUtils.hasText(username) && username.length() <= USERNAME_LENGTH_MAX) {
            return;
        }
        throw new MemberCreateException("Invalid username!");
    }

    private void validateEmail(String email) {
        if (isEmpty(email) || email.length() > EMAIL_LENGTH_MAX) {
            throw new MemberCreateException("Invalid email!");
        }
        if (memberRepository.existsByEmail(email)) {
            throw new MemberCreateException("The Email is already used.");
        }
    }

    @Transactional
    public void deleteMember(Long memberId) {
        if (memberId == null) {
            throw new MemberDeleteException("Member id cannot be null!");
        }

        Member deleteMember = memberRepository
                .findById(memberId)
                .orElseThrow(MemberDeleteException::causeByMemberNotFound);

        deleteMember.delete();
    }

    @Transactional
    public MemberResponse updateMemberWithProfileImage(MemberUpdateRequest memberUpdateRequest, MultipartFile profileImage) {
        String profileImagePath = createImagePath(Objects.requireNonNull(profileImage));
        
        storageService.saveFilesAtPath(Pair.of(profileImage, profileImagePath));
        
        Member updateMember = findByIdOrEmail(memberUpdateRequest);

        updateMember.update(
                Objects.requireNonNull(memberUpdateRequest.username()),
                profileImagePath,
                memberUpdateRequest.introduction()
        );

        return MemberResponse.from(updateMember);
    }

    @Transactional
    public MemberResponse updateMember(MemberUpdateRequest memberUpdateRequest) {
        Member updateMember = findByIdOrEmail(memberUpdateRequest);

        updateMember.update(
                Objects.requireNonNull(memberUpdateRequest.username()),
                updateMember.getProfileImagePath(),
                memberUpdateRequest.introduction()
        );

        return MemberResponse.from(updateMember);
    }

    private String createImagePath(MultipartFile file) {
        FileUtils.SupportedFileType fileType = FileUtils.getFileType(file);
        if (FileUtils.SupportedFileType.JPG.equals(fileType)) {
            return PROFILE_IMAGE_PATH_PREFIX + "/" + FileUtils.getFileNameByCurrentTime() + fileType.getExtension();
        }
        throw new MemberUpdateException("Content/type of profile image is jpeg.");
    }

    private Member findByIdOrEmail(MemberUpdateRequest memberUpdateRequest) {
        Member updateMember;
        if (memberUpdateRequest.id() == null) {
            updateMember = memberRepository
                    .findByEmail(memberUpdateRequest.email())
                    .orElseThrow(MemberUpdateException::causeByMemberNotFound);
        } else {
            updateMember = memberRepository
                    .findById(memberUpdateRequest.id())
                    .orElseThrow(MemberUpdateException::causeByMemberNotFound);
        }
        return updateMember;
    }

    @Transactional(readOnly = true)
    public Slice<MemberResponse> searchMembers(String keyword) {
        if (isEmpty(keyword)) {
            throw new MemberSearchException("No keyword for search.");
        }

        Pageable page = PageRequest.of(0, PAGE_SIZE, Sort.by("username"));

        Slice<Member> memberEntitySlice = memberRepository.findMembersByUsernameMatches(keyword, page);

        List<MemberResponse> foundMembers = memberEntitySlice.stream()
                .map(MemberResponse::from)
                .toList();

        return new SliceImpl<>(foundMembers, memberEntitySlice.getPageable(), memberEntitySlice.hasNext());
    }

    private boolean isEmpty(String keyword) {
        return !StringUtils.hasText(keyword);
    }
}
