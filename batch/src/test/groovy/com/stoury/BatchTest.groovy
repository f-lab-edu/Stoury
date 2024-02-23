package com.stoury

import com.stoury.domain.Diary
import com.stoury.domain.Feed
import com.stoury.domain.GraphicContent
import com.stoury.domain.Like
import com.stoury.domain.Member
import com.stoury.repository.DiaryRepository
import com.stoury.repository.FeedRepository
import com.stoury.repository.LikeRepository
import com.stoury.repository.MemberRepository
import com.stoury.repository.RankingRepository
import com.stoury.utils.cachekeys.FeedLikesCountSnapshotKeys
import com.stoury.utils.cachekeys.PopularSpotsKey
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

import static com.stoury.utils.cachekeys.HotFeedsKeys.DAILY_HOT_FEEDS
import static com.stoury.utils.cachekeys.HotFeedsKeys.MONTHLY_HOT_FEEDS
import static com.stoury.utils.cachekeys.HotFeedsKeys.WEEKLY_HOT_FEEDS

@SpringBootTest(classes = StouryBatchApplication.class)
@SpringBatchTest
@ActiveProfiles("test")
class BatchTest extends Specification {
    @Autowired
    JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired
    Job updatePopularSpotsJob
    @Autowired
    Job updateDailyFeedsJob
    @Autowired
    Job updateWeeklyFeedsJob
    @Autowired
    Job updateMonthlyFeedsJob
    @Autowired
    Job updateYearlyDiariesJob

    @Autowired
    MemberRepository memberRepository
    @Autowired
    FeedRepository feedRepository
    @Autowired
    RankingRepository rankingRepository
    @Autowired
    LikeRepository likeRepository
    @Autowired
    DiaryRepository diaryRepository

    @Autowired
    StringRedisTemplate redisTemplate

    def member = new Member("aaa@dddd.com", "qwdqwdqwd", "username", null)

    def setup() {
        feedRepository.deleteAll()
        diaryRepository.deleteAll()
        memberRepository.deleteAll()
        memberRepository.save(member)

        Set<String> allKeys = redisTemplate.keys("*")
        redisTemplate.delete(allKeys)
    }

    def cleanup() {
        feedRepository.deleteAll()
        diaryRepository.deleteAll()
        memberRepository.deleteAll()

        Set<String> allKeys = redisTemplate.keys("*")
        redisTemplate.delete(allKeys)
    }

    def "인기 여행지 업데이트 테스트"() {
        given:
        jobLauncherTestUtils.setJob(updatePopularSpotsJob)
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
        !rankingRepository.getRankedLocations(PopularSpotsKey.POPULAR_ABROAD_SPOTS).isEmpty()
    }

    def "일간 인기 피드 업데이트 테스트"() {
        given:
        jobLauncherTestUtils.setJob(updateDailyFeedsJob)
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
        !rankingRepository.getRankedFeeds(DAILY_HOT_FEEDS).isEmpty()
        likeRepository.getCountSnapshotByFeed(feed.id.toString(), ChronoUnit.DAYS) == 0
    }

    def "주간 인기 피드 업데이트 테스트"() {
        given:
        jobLauncherTestUtils.setJob(updateWeeklyFeedsJob)
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
        !rankingRepository.getRankedFeeds(WEEKLY_HOT_FEEDS).isEmpty()
        likeRepository.getCountSnapshotByFeed(feed.id.toString(), ChronoUnit.WEEKS) == 0
    }

    def "월간 인기 피드 업데이트 테스트"() {
        given:
        jobLauncherTestUtils.setJob(updateMonthlyFeedsJob)
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
        !rankingRepository.getRankedFeeds(MONTHLY_HOT_FEEDS).isEmpty()
        likeRepository.getCountSnapshotByFeed(feed.id.toString(), ChronoUnit.MONTHS) == 0
    }

    def "연간 인기 여행일지 업데이트 테스트"() {
        given:
        jobLauncherTestUtils.setJob(updateYearlyDiariesJob)
        def feeds = feedRepository.saveAll([feed(1), feed(2), feed(3)])
        def diary = new Diary(member, feeds, "test diary", feeds.get(0).graphicContents.first())
        diaryRepository.save(diary)
        feeds.forEach(feed -> likeRepository.save(new Like(member, feed)))
        expect:
        def jobExecution = jobLauncherTestUtils.launchJob()
        "COMPLETED" == jobExecution.getExitStatus().getExitCode()
        !rankingRepository.getRankedDiaries().isEmpty()
        feeds.stream().allMatch(feed ->
                likeRepository.getCountSnapshotByFeed(feed.id.toString(), ChronoUnit.YEARS) == 0)
    }

    def feed(long num){
        def feed = Feed.builder()
                .member(member)
                .textContent("feed"+num)
                .latitude(0)
                .longitude(0)
                .city("city" + num)
                .country("country" + num)
                .build()
        feed.addGraphicContent(new GraphicContent("/image/" + num + "jpeg", 0))
        return feed
    }

    def "일간 좋아요 수 스냅샷 읽으면 0으로 초기화"() {
        given:
        jobLauncherTestUtils.setJob(updateDailyFeedsJob)
        def feedId = feedRepository.save(feed(1)).id.toString()
        redisTemplate.opsForValue().set(FeedLikesCountSnapshotKeys.getCountSnapshotKey(ChronoUnit.DAYS, feedId), "3")
        when:
        jobLauncherTestUtils.launchJob()
        then:
        likeRepository.getCountSnapshotByFeed(feedId, ChronoUnit.DAYS) == 0
    }

    def "주간 좋아요 수 스냅샷 읽으면 0으로 초기화"() {
        given:
        jobLauncherTestUtils.setJob(updateWeeklyFeedsJob)
        def feedId = feedRepository.save(feed(1)).id.toString()
        redisTemplate.opsForValue().set(FeedLikesCountSnapshotKeys.getCountSnapshotKey(ChronoUnit.WEEKS, feedId), "3")
        when:
        jobLauncherTestUtils.launchJob()
        then:
        likeRepository.getCountSnapshotByFeed(feedId, ChronoUnit.WEEKS) == 0
    }

    def "월간 좋아요 수 스냅샷 읽으면 0으로 초기화"() {
        given:
        jobLauncherTestUtils.setJob(updateMonthlyFeedsJob)
        def feedId = feedRepository.save(feed(1)).id.toString()
        redisTemplate.opsForValue().set(FeedLikesCountSnapshotKeys.getCountSnapshotKey(ChronoUnit.MONTHS, feedId), "3")
        when:
        jobLauncherTestUtils.launchJob()
        then:
        likeRepository.getCountSnapshotByFeed(feedId, ChronoUnit.MONTHS) == 0
    }

    def "연간 좋아요 수 스냅샷 읽으면 0으로 초기화"() {
        given:
        jobLauncherTestUtils.setJob(updateYearlyDiariesJob)
        def feeds = feedRepository.saveAll([feed(1), feed(2), feed(3)])
        def diary = new Diary(member, feeds, "test diary", feeds.get(0).graphicContents.first())
        diaryRepository.save(diary)

        def feedId = feeds.get(0).id.toString()

        redisTemplate.opsForValue().set(FeedLikesCountSnapshotKeys.getCountSnapshotKey(ChronoUnit.YEARS, feedId), "3")
        when:
        jobLauncherTestUtils.launchJob()
        then:
        likeRepository.getCountSnapshotByFeed(feedId, ChronoUnit.YEARS) == 0
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
}
