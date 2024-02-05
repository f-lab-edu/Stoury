package com.stoury.service

import com.stoury.domain.Feed
import com.stoury.repository.FeedRepository
import com.stoury.repository.LikeRepository
import com.stoury.repository.RankingRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import spock.lang.Specification

import java.time.temporal.ChronoUnit
import java.util.stream.IntStream

class RankingServiceTest extends Specification {
    def feedRepository = Mock(FeedRepository)
    def rankingRepository = Mock(RankingRepository)
    def likeRepository = Mock(LikeRepository)
    def rankingService = new RankingService(feedRepository, rankingRepository, likeRepository)

    def setup() {
        likeRepository.existsByFeed(_ as Feed) >> true
    }

    def "인기 피드 업데이트"() {
        given:
        def feed1 = new Feed()
        feed1.id = 1L
        def feed2 = new Feed()
        feed2.id = 2L
        likeRepository.getCountByFeed(feed1) >> 10
        likeRepository.getCountSnapshotByFeed(feed1, _ as ChronoUnit) >> 5
        when:
        rankingService.updateHotFeed(ChronoUnit.DAYS, feed1)
        rankingService.updateHotFeed(ChronoUnit.DAYS, feed2)
        then:
        1 * rankingRepository.saveHotFeed(feed1.id.toString(), _, _)
        0 * rankingRepository.saveHotFeed(feed2.id.toString(), _, _)
    }
}
