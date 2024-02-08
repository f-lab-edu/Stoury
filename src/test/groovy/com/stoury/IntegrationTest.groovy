package com.stoury

import com.stoury.domain.Feed
import com.stoury.domain.Like
import com.stoury.domain.Member
import com.stoury.dto.member.MemberResponse
import com.stoury.repository.FeedRepository
import com.stoury.repository.LikeRepository
import com.stoury.repository.MemberRepository
import com.stoury.repository.RankingRepository
import com.stoury.service.MemberService
import com.stoury.utils.CacheKeys
import org.springframework.batch.core.Job
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Slice
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import java.time.temporal.ChronoUnit
import java.util.stream.IntStream

@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
class IntegrationTest extends Specification {
    @Autowired
    JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired
    Job jobUpdatePopularSpots
    @Autowired
    Job jobDailyFeed
    @Autowired
    Job jobWeeklyFeed
    @Autowired
    Job jobMonthlyFeed
    @Autowired
    FeedRepository feedRepository
    @Autowired
    MemberRepository memberRepository
    @Autowired
    LikeRepository likeRepository
    @Autowired
    RankingRepository rankingRepository
    @Autowired
    StringRedisTemplate redisTemplate
    @Autowired
    MemberService memberService

    def member = new Member("aaa@dddd.com", "qwdqwdqwd", "username", null);

    def setup() {
        feedRepository.deleteAll()
        memberRepository.deleteAll()
        memberRepository.save(member)

        Set<String> allKeys = redisTemplate.keys("*")
        redisTemplate.delete(allKeys);
    }

    def cleanup() {
        feedRepository.deleteAll()
        memberRepository.deleteAll()

        Set<String> allKeys = redisTemplate.keys("*")
        redisTemplate.delete(allKeys)
    }

    def "인기 여행지 업데이트 테스트"() {
        given:
        jobLauncherTestUtils.setJob(jobUpdatePopularSpots)
        def feed = Feed.builder()
                .member(member)
                .textContent("blabla")
                .latitude(0)
                .longitude(0)
                .city("city")
                .country("country")
                .build()
        feedRepository.saveAndFlush(feed)
        when:
        def jobExecution = jobLauncherTestUtils.launchJob()
        then:
        "COMPLETED" == jobExecution.getExitStatus().getExitCode()
        !rankingRepository.getRankedLocations(CacheKeys.POPULAR_ABROAD_SPOTS).isEmpty()
    }

    def "일간 인기 피드 업데이트 테스트"() {
        given:
        jobLauncherTestUtils.setJob(jobDailyFeed)
        def feed = Feed.builder()
                .member(member)
                .textContent("blabla")
                .latitude(0)
                .longitude(0)
                .city("city")
                .country("country")
                .build()
        def savedFeed = feedRepository.saveAndFlush(feed)
        likeRepository.save(new Like(member, savedFeed))
        expect:
        def jobExecution = jobLauncherTestUtils.launchJob()
        "COMPLETED" == jobExecution.getExitStatus().getExitCode()
        !rankingRepository.getRankedFeedIds(CacheKeys.DAILY_HOT_FEEDS).isEmpty()
    }

    def "주간 인기 피드 업데이트 테스트"() {
        given:
        jobLauncherTestUtils.setJob(jobWeeklyFeed)
        def feed = Feed.builder()
                .member(member)
                .textContent("blabla")
                .latitude(0)
                .longitude(0)
                .city("city")
                .country("country")
                .build()
        def savedFeed = feedRepository.saveAndFlush(feed)
        likeRepository.save(new Like(member, savedFeed))
        expect:
        def jobExecution = jobLauncherTestUtils.launchJob()
        "COMPLETED" == jobExecution.getExitStatus().getExitCode()
        !rankingRepository.getRankedFeedIds(CacheKeys.WEEKLY_HOT_FEEDS).isEmpty()
    }

    def "월간 인기 피드 업데이트 테스트"() {
        given:
        jobLauncherTestUtils.setJob(jobMonthlyFeed)
        def feed = Feed.builder()
                .member(member)
                .textContent("blabla")
                .latitude(0)
                .longitude(0)
                .city("city")
                .country("country")
                .build()
        def savedFeed = feedRepository.saveAndFlush(feed)
        likeRepository.save(new Like(member, savedFeed))
        expect:
        def jobExecution = jobLauncherTestUtils.launchJob()
        "COMPLETED" == jobExecution.getExitStatus().getExitCode()
        !rankingRepository.getRankedFeedIds(CacheKeys.MONTHLY_HOT_FEEDS).isEmpty()
    }

    def "해외에서 10개 인기 여행장소"() {
        given:
        (0..<3).each { i ->
            def feed = new Feed(member, "feed#" + i + 8, 11.11, 11.11, Collections.emptyList(), "city", "country")
            feed.country = "France"
            feed.city = "Paris"
            feedRepository.save(feed)
        }
        (0..<4).each { i ->
            def feed = new Feed(member, "feed#" + i, 11.11, 11.11, Collections.emptyList(), "city", "country")
            feed.country = "United States"
            feed.city = "NY"
            feedRepository.save(feed)
        }
        (0..<1).each { i ->
            def feed = new Feed(member, "feed#" + i + 12, 11.11, 11.11, Collections.emptyList(), "city", "country")
            feed.country = "Vietnam"
            feed.city = "hanoi"
            feedRepository.save(feed)
        }
        (0..<2).each { i ->
            def feed = new Feed(member, "feed#" + i + 12, 11.11, 11.11, Collections.emptyList(), "city", "country")
            feed.country = "Australia"
            feed.city = "Sydney"
            feedRepository.save(feed)
        }
        def page = PageRequest.of(0, 10)

        when:
        def feeds = feedRepository.findTop10CountriesNotKorea(page)
        then:
        feeds.get(0) == "United States"
        feeds.get(1) == "France"
        feeds.get(2) == "Australia"
        feeds.get(3) == "Vietnam"
    }

    def "국내에서 10개 인기 여행장소"() {
        given:
        (0..<3).each { i ->
            def feed = new Feed(member, "feed#" + i + 8, 11.11, 11.11, Collections.emptyList(), "city", "country")
            feed.country = "South Korea"
            feed.city = "Seoul"
            feedRepository.save(feed)
        }
        (0..<4).each { i ->
            def feed = new Feed(member, "feed#" + i, 11.11, 11.11, Collections.emptyList(), "city", "country")
            feed.country = "South Korea"
            feed.city = "Busan"
            feedRepository.save(feed)
        }
        (0..<1).each { i ->
            def feed = new Feed(member, "feed#" + i + 12, 11.11, 11.11, Collections.emptyList(), "city", "country")
            feed.country = "South Korea"
            feed.city = "Hwacheon"
            feedRepository.save(feed)
        }
        (0..<2).each { i ->
            def feed = new Feed(member, "feed#" + i + 12, 11.11, 11.11, Collections.emptyList(), "city", "country")
            feed.country = "South Korea"
            feed.city = "Daejeon"
            feedRepository.save(feed)
        }
        def page = PageRequest.of(0, 10)

        when:
        def feeds = feedRepository.findTop10CitiesInKorea(page)
        then:
        feeds.get(0) == "Busan"
        feeds.get(1) == "Seoul"
        feeds.get(2) == "Daejeon"
        feeds.get(3) == "Hwacheon"
    }

    def "좋아요 저장 성공"() {
        given:
        def member = new Member()
        def feed = new Feed()
        member.id = 1L
        feed.id = 2L
        def like = new Like(member, feed)

        when:
        likeRepository.save(like)
        then:
        likeRepository.existsByMemberAndFeed(member, feed)
    }

    def "피드의 좋아요 개수 가져오기"() {
        given:
        def member1 = new Member()
        def member2 = new Member()
        def member3 = new Member()
        def feed = new Feed()
        member1.id = 1L
        member2.id = 2L
        member3.id = 3L
        feed.id = 1L

        likeRepository.save(new Like(member1, feed))
        likeRepository.save(new Like(member2, feed))
        likeRepository.save(new Like(member3, feed))

        when:
        def likes = likeRepository.getCountByFeedId("1")
        then:
        likes == 3
    }

    def "좋아요 삭제 - 성공"() {
        given:
        def member = new Member()
        def feed = new Feed()
        member.id = 1L
        feed.id = 2L
        likeRepository.save(new Like(member, feed))

        expect:
        likeRepository.deleteByMemberAndFeed("1", "2")
        !likeRepository.existsByMemberAndFeed(member, feed)
    }

    def "좋아요 삭제 - 실패, 없는 데이터 삭제 시도"() {
        expect:
        !likeRepository.deleteByMemberAndFeed("1", "2")
    }

    def "인기 피드 랭킹"() {
        given:
        def likeIncreases = [
                //0    1   2   3    4
                5, 13, 1, 100, 2,
                //5    6   7   8    9
                111, 32, 23, 8, 3,
                //10   11  12  13   14
                10, 24, 98, 55, 101,
                //15   16  17  18   19
                333, 31, 56, 74, 6
        ]
        when:
        IntStream.range(0, 20)
                .forEach(i -> rankingRepository.saveHotFeed(String.valueOf(i), likeIncreases.get(i), ChronoUnit.DAYS))

        then:
        def rankedList = rankingRepository.getRankedFeedIds(CacheKeys.getHotFeedsKey(ChronoUnit.DAYS))
        def expectedList = List.of(
                15, 5, 14, 3, 12,
                18, 17, 13, 6, 16,
                11, 7, 1, 10, 8,
                19, 0, 9, 4, 2)
        (0..<20).each { i ->
            rankedList.get(i) == expectedList.get(i)
        }
    }

    def "사용자 검색"() {
        given:
        Member member1 = Member.builder().email("mem1@aaaa.com").encryptedPassword("pwdpwdpwdpwd").username("member1").build();
        Member member2 = Member.builder().email("mem2@aaaa.com").encryptedPassword("pwdpwdpwdpwd").username("member2").build();
        Member member3 = Member.builder().email("mem3@aaaa.com").encryptedPassword("pwdpwdpwdpwd").username("mexber3").build();
        Member member4 = Member.builder().email("mem4@aaaa.com").encryptedPassword("pwdpwdpwdpwd").username("xember4").build();
        Member member5 = Member.builder().email("mem5@aaaa.com").encryptedPassword("pwdpwdpwdpwd").username("member5").build();
        Member member6 = Member.builder().email("mem6@aaaa.com").encryptedPassword("pwdpwdpwdpwd").username("member6").build();
        Member member7 = Member.builder().email("mem7@aaaa.com").encryptedPassword("pwdpwdpwdpwd").username("member7").build();
        Member member8 = Member.builder().email("mem8@aaaa.com").encryptedPassword("pwdpwdpwdpwd").username("member8").build();
        memberRepository.saveAll(List.of(member1, member2, member3, member4, member5, member6, member7, member8));

        when:
        Slice<MemberResponse> slice = memberService.searchMembers("mem");
        List<MemberResponse> foundMembers = slice.getContent();

        then:
        slice.size == MemberService.PAGE_SIZE
        slice.hasNext()
        foundMembers.get(0).username()==member1.getUsername()
        foundMembers.get(1).username()==member2.getUsername()
        foundMembers.get(2).username()==member5.getUsername()
        foundMembers.get(3).username()==member6.getUsername()
        foundMembers.get(4).username()==member7.getUsername()
    }
}
