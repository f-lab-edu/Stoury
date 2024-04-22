package com.stoury.service;

import com.stoury.domain.Follow;
import com.stoury.domain.Member;
import com.stoury.dto.SimpleMemberResponse;
import com.stoury.exception.member.MemberSearchException;
import com.stoury.repository.FollowRepository;
import com.stoury.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;

    @Transactional
    public void follow(Long followerId, String followeeEmail) {
        Member follower = memberRepository.findById(followerId).orElseThrow(MemberSearchException::new);
        Member followee = memberRepository.findByEmail(followeeEmail).orElseThrow(MemberSearchException::new);

        Follow follow = new Follow(follower, followee);
        followRepository.save(follow);
    }

    @Transactional
    public List<SimpleMemberResponse> getFollowingMembers(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(MemberSearchException::new);

        List<Member> followingMembers = memberRepository.findByFollowersContain(member);

        return SimpleMemberResponse.from(followingMembers);
    }
}
