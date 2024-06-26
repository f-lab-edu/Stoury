package com.stoury.service;

import com.stoury.domain.Member;
import com.stoury.dto.member.AuthenticatedMember;
import com.stoury.dto.member.MemberCreateRequest;
import com.stoury.dto.member.MemberResponse;
import com.stoury.dto.member.MemberUpdateRequest;
import com.stoury.dto.member.OnlineMember;
import com.stoury.exception.member.MemberCreateException;
import com.stoury.exception.member.MemberDeleteException;
import com.stoury.exception.member.MemberSearchException;
import com.stoury.exception.member.MemberUpdateException;
import com.stoury.repository.MemberOnlineStatusRepository;
import com.stoury.repository.MemberRepository;
import com.stoury.service.storage.StorageService;
import com.stoury.utils.FileUtils;
import com.stoury.utils.SupportedFileType;
import com.stoury.utils.PageSize;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.geo.Point;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.stoury.utils.Values.MEMBER_ID_NOT_NULL_MESSAGE;

@Service
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {
    private final MemberRepository memberRepository;
    private final MemberOnlineStatusRepository memberOnlineStatusRepository;
    private final PasswordEncoder passwordEncoder;
    private final StorageService storageService;
    public static final int PASSWORD_LENGTH_MIN = 8;
    public static final int PASSWORD_LENGTH_MAX = 30;
    public static final int USERNAME_LENGTH_MAX = 10;
    public static final int EMAIL_LENGTH_MAX = 25;
    @Value("${profileImage.path-prefix}")
    public String profileImagePathPrefix;

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
            throw new MemberDeleteException(MEMBER_ID_NOT_NULL_MESSAGE);
        }

        Member deleteMember = memberRepository
                .findById(memberId)
                .orElseThrow(MemberDeleteException::causeByMemberNotFound);

        deleteMember.delete();
    }

    @Transactional
    public MemberResponse updateMemberWithProfileImage(MemberUpdateRequest memberUpdateRequest, MultipartFile profileImage) {
        validate(profileImage);

        String profileImagePath = FileUtils.createFilePath(profileImage, profileImagePathPrefix);

        storageService.saveFileAtPath(profileImage, Paths.get(profileImagePath));

        Member updateMember = findByIdOrEmail(memberUpdateRequest);

        updateMember.update(
                Objects.requireNonNull(memberUpdateRequest.username()),
                profileImagePath,
                memberUpdateRequest.introduction()
        );

        return MemberResponse.from(updateMember);
    }

    public Member getMemberByEmail(String email) {
        return memberRepository.findByEmail(Objects.requireNonNull(email))
                .orElseThrow(() -> new UsernameNotFoundException("Cannot find member"));
    }

    private void validate(MultipartFile file) {
        if (SupportedFileType.isUnsupportedFile(file)) {
            throw new MemberUpdateException("Content/type of profile image is jpeg.");
        }
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

        Pageable page = PageRequest.of(0, PageSize.MEMBER_PAGE_SIZE, Sort.by("username"));

        Slice<Member> memberEntitySlice = memberRepository.findMembersByUsernameMatches(keyword, page);

        List<MemberResponse> foundMembers = memberEntitySlice.stream()
                .map(MemberResponse::from)
                .toList();

        return new SliceImpl<>(foundMembers, memberEntitySlice.getPageable(), memberEntitySlice.hasNext());
    }

    private boolean isEmpty(String keyword) {
        return !StringUtils.hasText(keyword);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = getMemberByEmail(email);

        return AuthenticatedMember.from(member);
    }

    public void setOnline(Long memberId, Double latitude, Double longitude) {
        memberOnlineStatusRepository.save(memberId, latitude, longitude);
    }

    public void setOffline(Long memberId) {
        memberOnlineStatusRepository.delete(memberId);
    }

    @Transactional(readOnly = true)
    public List<OnlineMember> searchOnlineMembers(Long memberId, Double latitude, Double longitude, double radiusKm) {
        long memberIdNotNull = Objects.requireNonNull(memberId, MEMBER_ID_NOT_NULL_MESSAGE);
        double latitudeNotNull = Objects.requireNonNull(latitude, "Invalid points information.");
        double longitudeNotNull = Objects.requireNonNull(longitude, "Invalid points information.");

        Map<Long, Integer> memberDistances = memberOnlineStatusRepository
                .findByPoint(memberIdNotNull, new Point(longitudeNotNull, latitudeNotNull), radiusKm);
        Set<Long> memberIds = memberDistances.keySet();

        return memberRepository.findAllById(memberIds).stream()
                .map(member -> OnlineMember.from(member, memberDistances.get(member.getId())))
                .sorted(Comparator.comparingInt(OnlineMember::distance))
                .toList();
    }
}
