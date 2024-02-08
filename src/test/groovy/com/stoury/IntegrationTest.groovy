package com.stoury

import com.stoury.domain.Feed
import com.stoury.domain.Like
import com.stoury.domain.Member
import com.stoury.repository.FeedRepository
import com.stoury.repository.LikeRepository
import com.stoury.repository.MemberRepository
import com.stoury.repository.RankingRepository
import com.stoury.utils.CacheKeys
import org.springframework.batch.core.Job
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
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
    FeedRepository feedRepository
    @Autowired
    MemberRepository memberRepository
    @Autowired
    LikeRepository likeRepository
    @Autowired
    RankingRepository rankingRepository
    @Autowired
    StringRedisTemplate redisTemplate

    def member = new Member("aaa@dddd.com", "qwdqwdqwd", "username", null);

    def setup() {
        feedRepository.deleteAll()
        memberRepository.deleteAll()
        memberRepository.save(member)

        Set<String> allKeys = redisTemplate.keys("*");
        redisTemplate.delete(allKeys);
    }

    def cleanup() {
        feedRepository.deleteAll()
        memberRepository.deleteAll()

        Set<String> allKeys = redisTemplate.keys("*");
        redisTemplate.delete(allKeys);
    }


    def "인기 여행지 업데이트 테스트"() {
        given:
        jobLauncherTestUtils.setJob(jobUpdatePopularSpots);
        when:
        def jobExecution = jobLauncherTestUtils.launchJob();
        then:
        "COMPLETED" == jobExecution.getExitStatus().getExitCode()
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
                5,   13, 1,  100, 2,
                //5    6   7   8    9
                111, 32, 23, 8,   3,
                //10   11  12  13   14
                10,  24, 98, 55,  101,
                //15   16  17  18   19
                333, 31, 56, 74,  6
        ]
        when:
        IntStream.range(0, 20)
                .forEach(i -> rankingRepository.saveHotFeed(String.valueOf(i), likeIncreases.get(i), ChronoUnit.DAYS))

        then:
        def rankedList = rankingRepository.getRankedFeedIds(CacheKeys.getHotFeedsKey(ChronoUnit.DAYS))
        def expectedList = List.of(
                15, 5,  14, 3,  12,
                18, 17, 13, 6,  16,
                11, 7,  1,  10, 8,
                19, 0,  9,  4,  2)
        (0..<20).each { i ->
            rankedList.get(i) == expectedList.get(i)
        }
    }
}
