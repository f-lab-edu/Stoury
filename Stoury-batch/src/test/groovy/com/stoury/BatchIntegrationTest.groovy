package com.stoury

import com.stoury.batch.BatchRecommendFeedsConfig
import com.stoury.domain.Diary
import com.stoury.domain.Feed
import com.stoury.domain.GraphicContent
import com.stoury.domain.Like
import com.stoury.domain.Member
import com.stoury.domain.Tag
import com.stoury.projection.FeedResponseEntity
import com.stoury.repository.DiaryRepository
import com.stoury.repository.FeedRepository
import com.stoury.repository.LikeRepository
import com.stoury.repository.MemberRepository
import com.stoury.repository.RankingRepository
import com.stoury.repository.RecommendFeedsRepository
import com.stoury.repository.TagRepository
import com.stoury.utils.cachekeys.FeedLikesCountSnapshotKeys
import com.stoury.utils.cachekeys.FrequentTagsKey
import com.stoury.utils.cachekeys.PopularSpotsKey
import com.stoury.utils.cachekeys.RecommendFeedsKey
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
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
class BatchIntegrationTest extends Specification {
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
    Step getRecommendFeedsStep
    @Autowired
    Step getFrequentTagsStep
    @Autowired
    Job updateRecommendFeedsJob
    @Autowired
    BatchRecommendFeedsConfig batchRecommendFeedsConfig

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
    RecommendFeedsRepository recommendFeedsRepository
    @Autowired
    TagRepository tagRepository

    @Autowired
    StringRedisTemplate redisTemplate

    def member = new Member("aaa@dddd.com", "qwdqwdqwd", "username", null)

    def setup() {
        feedRepository.deleteAllFeedResponse()
        feedRepository.deleteAll()
        tagRepository.deleteAll()
        diaryRepository.deleteAll()
        memberRepository.deleteAll()
        memberRepository.save(member)

        Set<String> allKeys = redisTemplate.keys("*")
        redisTemplate.delete(allKeys)
    }

    def cleanup() {
        feedRepository.deleteAllFeedResponse()
        feedRepository.deleteAll()
        tagRepository.deleteAll()
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
        when:
        def jobExecution = jobLauncherTestUtils.launchJob()
        then:
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
        when:
        def jobExecution = jobLauncherTestUtils.launchJob()
        then:
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
        when:
        def jobExecution = jobLauncherTestUtils.launchJob()
        then:
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
        when:
        def jobExecution = jobLauncherTestUtils.launchJob()
        then:
        "COMPLETED" == jobExecution.getExitStatus().getExitCode()
        !rankingRepository.getRankedDiaries().isEmpty()
        feeds.stream().allMatch(feed ->
                likeRepository.getCountSnapshotByFeed(feed.id.toString(), ChronoUnit.YEARS) == 0)
    }

    def feed(long num) {
        def feed = Feed.builder()
                .member(member)
                .textContent("feed" + num)
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
            def feed = new Feed(member, "feed#" + i + 8, 11.11, 11.11, [] as Set, "city", "country")
            feed.country = "France"
            feed.city = "Paris"
            feedRepository.save(feed)
        }
        (0..<4).each { i ->
            def feed = new Feed(member, "feed#" + i, 11.11, 11.11, [] as Set, "city", "country")
            feed.country = "United States"
            feed.city = "NY"
            feedRepository.save(feed)
        }
        (0..<1).each { i ->
            def feed = new Feed(member, "feed#" + i + 12, 11.11, 11.11, [] as Set, "city", "country")
            feed.country = "Vietnam"
            feed.city = "hanoi"
            feedRepository.save(feed)
        }
        (0..<2).each { i ->
            def feed = new Feed(member, "feed#" + i + 12, 11.11, 11.11, [] as Set, "city", "country")
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
            def feed = new Feed(member, "feed#" + i + 8, 11.11, 11.11, [] as Set, "city", "country")
            feed.country = "South Korea"
            feed.city = "Seoul"
            feedRepository.save(feed)
        }
        (0..<4).each { i ->
            def feed = new Feed(member, "feed#" + i, 11.11, 11.11, [] as Set, "city", "country")
            feed.country = "South Korea"
            feed.city = "Busan"
            feedRepository.save(feed)
        }
        (0..<1).each { i ->
            def feed = new Feed(member, "feed#" + i + 12, 11.11, 11.11, [] as Set, "city", "country")
            feed.country = "South Korea"
            feed.city = "Hwacheon"
            feedRepository.save(feed)
        }
        (0..<2).each { i ->
            def feed = new Feed(member, "feed#" + i + 12, 11.11, 11.11, [] as Set, "city", "country")
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

    def "맞춤피드 업데이트 테스트"() {
        given:
        def member1 = memberRepository.save(new Member("test1@test.com", "qweqwe", "user1", null))
        def member2 = memberRepository.save(new Member("test2@test.com", "qweqwe", "user2", null))
        def member3 = memberRepository.save(new Member("test3@test.com", "qweqwe", "user3", null))

        def feedResponses = [
                new FeedResponseEntity(feedId: 1L, tagNames: '[ "tag1", "tag2", "tag3" ]'),
                new FeedResponseEntity(feedId: 2L, tagNames: '[ "tag2", "tag3", "tag4" ]'),
                new FeedResponseEntity(feedId: 3L, tagNames: '[ "tag3", "tag4", "tag5" ]'),
                new FeedResponseEntity(feedId: 4L, tagNames: '[ "tag2", "tag3", "tag1" ]'),
                new FeedResponseEntity(feedId: 5L, tagNames: '[ "tag6", "tag7", "tag9" ]'),
                new FeedResponseEntity(feedId: 6L, tagNames: '[ "tag11", "tag8", "tag10" ]'),
                new FeedResponseEntity(feedId: 7L, tagNames: '[ "tag12", "tag11", "tag2" ]'),
                new FeedResponseEntity(feedId: 8L, tagNames: '[ "tag2", "tag8", "tag6" ]'),
                new FeedResponseEntity(feedId: 9L, tagNames: '[ "tag5", "tag10", "tag11" ]'),
                new FeedResponseEntity(feedId: 10L, tagNames: '[ "tag12", "tag13", "tag14" ]'),
        ]
        feedRepository.saveAllFeedResponses(feedResponses)

        List<Tag> tags = tagRepository.saveAll([
                new Tag("tag0"), new Tag("tag1"), new Tag("tag2"), new Tag("tag3"), new Tag("tag4"),
                new Tag("tag5"), new Tag("tag6"), new Tag("tag7"), new Tag("tag8"), new Tag("tag9"),
                new Tag("tag10"), new Tag("tag11"), new Tag("tag12"), new Tag("tag13"), new Tag("tag14"),
        ])
        def feeds = [
                new Feed(member, "", 0, 0, [tags.get(1), tags.get(2), tags.get(3)] as Set, "", ""),
                new Feed(member, "", 0, 0, [tags.get(2), tags.get(3), tags.get(4)] as Set, "", ""),
                new Feed(member, "", 0, 0, [tags.get(3), tags.get(4), tags.get(5)] as Set, "", ""),
                new Feed(member, "", 0, 0, [tags.get(2), tags.get(3), tags.get(1)] as Set, "", ""),
                new Feed(member, "", 0, 0, [tags.get(6), tags.get(7), tags.get(9)] as Set, "", ""),
                new Feed(member, "", 0, 0, [tags.get(11), tags.get(8), tags.get(10)] as Set, "", ""),
                new Feed(member, "", 0, 0, [tags.get(12), tags.get(11), tags.get(2)] as Set, "", ""),
                new Feed(member, "", 0, 0, [tags.get(2), tags.get(8), tags.get(6)] as Set, "", ""),
                new Feed(member, "", 0, 0, [tags.get(5), tags.get(10), tags.get(11)] as Set, "", ""),
                new Feed(member, "", 0, 0, [tags.get(12), tags.get(13), tags.get(14)] as Set, "", ""),
        ]
        feedRepository.saveAll(feeds)

        memberViewedFeeds(member1.id, 1L, 2L, 3L) // viewedTags = [ tag1, tag2, tag3, tag4, tag5 ]
        memberViewedFeeds(member2.id, 2L, 4L, 5L, 6L, 7L) // viewedTags = [ tag1, tag2, tag3, tag4, tag6, tag7, tag8, tag9, tag10, tag11, tag12 ]
        memberViewedFeeds(member3.id, 5L, 8L, 9L, 10L) // viewedTags = [ tag2, tag5, tag6, tag7, tag9, tag8, tag10, tag11, tag12, tag13, tag14 ]

        jobLauncherTestUtils.setJob(updateRecommendFeedsJob)
        when:
        def execution = jobLauncherTestUtils.launchJob()
        then:
        "COMPLETED" == execution.getExitStatus().getExitCode()
        redisTemplate.opsForSet().members(FrequentTagsKey.getFrequentTagsKey(member1.id.toString())).containsAll("tag1", "tag2", "tag3", "tag4", "tag5")
        redisTemplate.opsForSet().members(FrequentTagsKey.getFrequentTagsKey(member2.id.toString())).containsAll("tag1", "tag2", "tag3", "tag4", "tag6", "tag7", "tag8", "tag9", "tag10", "tag11", "tag12")
        redisTemplate.opsForSet().members(FrequentTagsKey.getFrequentTagsKey(member3.id.toString())).containsAll("tag2", "tag5", "tag6", "tag7", "tag9", "tag8", "tag10", "tag11", "tag12", "tag13", "tag14")
        !redisTemplate.opsForSet().members(RecommendFeedsKey.getRecommendFeedsKey(member1.id.toString())).isEmpty()
        !redisTemplate.opsForSet().members(RecommendFeedsKey.getRecommendFeedsKey(member2.id.toString())).isEmpty()
        !redisTemplate.opsForSet().members(RecommendFeedsKey.getRecommendFeedsKey(member3.id.toString())).isEmpty()
    }

    def memberViewedFeeds(Long memberId, Long... feedIds) {
        for (final def feedId in feedIds) {
            recommendFeedsRepository.addViewedFeed(memberId, feedId)
        }
    }
}
