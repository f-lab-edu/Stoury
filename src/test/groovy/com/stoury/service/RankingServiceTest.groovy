package com.stoury.service


import com.stoury.repository.FeedRepository
import com.stoury.repository.LikeRepository
import com.stoury.repository.RankingRepository
import spock.lang.Specification

import java.time.temporal.ChronoUnit

class RankingServiceTest extends Specification {
    def feedRepository = Mock(FeedRepository)
    def rankingRepository = Mock(RankingRepository)
    def likeRepository = Mock(LikeRepository)
    def rankingService = new RankingService(feedRepository, rankingRepository, likeRepository)

    def setup() {
        likeRepository.existsByFeedId(_) >> true
    }

    def "인기 피드 업데이트 - 좋아요수가 증가한 것만 순위에 적용"() {
        given:
        likeRepository.getCountByFeedId("1") >> 10
        likeRepository.getCountSnapshotByFeed("1", _ as ChronoUnit) >> 5
        when:
        rankingService.updateHotFeed(ChronoUnit.DAYS, "1")
        rankingService.updateHotFeed(ChronoUnit.DAYS, "2")
        then:
        1 * rankingRepository.saveHotFeed("1", _, _)
        0 * rankingRepository.saveHotFeed("2", _, _)
    }
}
