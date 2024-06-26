package com.stoury.service;

import com.stoury.domain.Follow;
import com.stoury.domain.Member;
import com.stoury.dto.SimpleMemberResponse;
import com.stoury.exception.MaxFollowingException;
import com.stoury.exception.member.MemberSearchException;
import com.stoury.repository.FollowRepository;
import com.stoury.repository.MemberRepository;
import com.stoury.utils.Values;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.stoury.utils.Values.MEMBER_ID_NOT_NULL_MESSAGE;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;

    @Transactional
    public void follow(Long followerId, String followeeEmail) {
        Long followerIdNonNull = Objects.requireNonNull(followerId, "followerId can't be null");
        String followeeEmailNonNull = Objects.requireNonNull(followeeEmail, "followeeEmail can't be null");

        Member follower = memberRepository.findById(followerIdNonNull).orElseThrow(MemberSearchException::new);

        checkFollowingCounts(follower);

        Member followee = memberRepository.findByEmail(followeeEmailNonNull).orElseThrow(MemberSearchException::new);


        Follow follow = new Follow(follower, followee);
        followRepository.save(follow);
    }

    private void checkFollowingCounts(Member follower) {
        long followingCounts = followRepository.countFollowingNumbersOf(follower);
        if (followingCounts >= Values.MAX_FOLLOWING) {
            throw new MaxFollowingException();
        }
    }

    @Transactional(readOnly = true)
    public List<SimpleMemberResponse> getFollowingMembers(Long memberId) {
        Long memberIdNonNull = Objects.requireNonNull(memberId, MEMBER_ID_NOT_NULL_MESSAGE);

        Member member = memberRepository.findById(memberIdNonNull).orElseThrow(MemberSearchException::new);

        List<Member> followingMembers = memberRepository.findByFollowersContain(member);

        return SimpleMemberResponse.from(followingMembers);
    }

    @Transactional(readOnly = true)
    public List<SimpleMemberResponse> getFollowersOfMember(Long memberId, String offsetUsername) {
        Long memberIdNonNull = Objects.requireNonNull(memberId, MEMBER_ID_NOT_NULL_MESSAGE);
        String offsetUsernameNonNull = offsetUsername == null ? "" : offsetUsername;

        Member member = memberRepository.findById(memberIdNonNull).orElseThrow(MemberSearchException::new);

        List<Member> followersOfMember = memberRepository.findByFollowee(member, offsetUsernameNonNull);

        return SimpleMemberResponse.from(followersOfMember);
    }
}
