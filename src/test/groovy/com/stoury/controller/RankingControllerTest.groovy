package com.stoury.controller

import com.stoury.dto.SimpleMemberResponse
import com.stoury.dto.diary.SimpleDiaryResponse
import com.stoury.dto.feed.SimpleFeedResponse
import com.stoury.service.RankingService
import org.spockframework.spring.SpringBean
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean

import java.time.temporal.ChronoUnit

import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.when
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(RankingController.class)
class RankingControllerTest extends AbstractRestDocsTests {
    @SpringBean
    RankingService rankingService = Mock()

    def simpleFeeds = [
            new SimpleFeedResponse(2, new SimpleMemberResponse(1, "writer1"), "Tokyo", "Japan"),
            new SimpleFeedResponse(5, new SimpleMemberResponse(2, "writer2"), "New York", "United States"),
            new SimpleFeedResponse(10, new SimpleMemberResponse(3, "writer3"), "London", "United Kingdom"),
            new SimpleFeedResponse(1, new SimpleMemberResponse(2, "writer2"), "Moscow", "Russia"),
            new SimpleFeedResponse(6, new SimpleMemberResponse(3, "writer3"), "Seoul", "South Korea"),
            new SimpleFeedResponse(8, new SimpleMemberResponse(4, "writer4"), "Shanghai", "China"),
            new SimpleFeedResponse(9, new SimpleMemberResponse(6, "writer6"), "Osaka", "Japan"),
            new SimpleFeedResponse(100, new SimpleMemberResponse(1, "writer1"), "Taipei", "Taiwan"),
            new SimpleFeedResponse(56, new SimpleMemberResponse(2, "writer2"), "Paris", "France"),
            new SimpleFeedResponse(102, new SimpleMemberResponse(2, "writer2"), "Hanoi", "Vietnam"),
    ]

    def simpleDiaries = [
            new SimpleDiaryResponse(2, "/image1.jpeg", "Travel of Chung", 1),
            new SimpleDiaryResponse(4, "/image2.jpeg", "Healing in japan", 3),
            new SimpleDiaryResponse(1, "/image3.jpeg", "Trip africa", 1),
            new SimpleDiaryResponse(5, "/image4.jpeg", "Amazing paris", 2),
            new SimpleDiaryResponse(99, "/image55.jpeg", "Tragedy of warsaw", 66),
            new SimpleDiaryResponse(123, "/image10.jpeg", "5 days in pallujah", 123),
            new SimpleDiaryResponse(23425, "/image14.jpeg", "Best or Worst? Canda", 45),
            new SimpleDiaryResponse(32, "/image102.jpeg", "Hiking alps", 132),
            new SimpleDiaryResponse(414, "/image67.jpeg", "The most modern city, milano", 12),
    ]

    def setup() {
        rankingService.getHotFeeds(_ as ChronoUnit) >> simpleFeeds
        rankingService.getHotDiaries() >> simpleDiaries
    }

    def "Get popular domestic spots"() {
        given:
        rankingService.getPopularDomesticSpots() >> ["Seoul-si", "Busan-si", "Incheon-si", "Daegu-si", "Daejeon-si",
                                                     "Gwangju-si", "Suwon-si", "Ulsan-si", "Sejong-si", "Jeju-si"]
        when:
        def response = mockMvc.perform(get("/rank/domestic-spots"))
                .andDo(document())
        then:
        response.andExpect(status().isOk())
    }

    def "Get popular abroad spots"() {
        given:
        rankingService.getPopularAbroadSpots() >> ["United States", "United Kingdom", "Canada", "Australia", "France",
                                                   "Germany", "Italy", "Spain", "Japan", "China"]
        when:
        def response = mockMvc.perform(get("/rank/abroad-spots"))
                .andDo(document())
        then:
        response.andExpect(status().isOk())
    }

    def "Get daily popular feeds"() {
        expect:
        mockMvc.perform(get("/rank/%s".formatted(duration)))
                .andDo(document("{class-name}/" + "get " + duration))
                .andExpect(status().isOk())
        where:
        duration            | _
        "daily-hot-feeds"   | _
        "weekly-hot-feeds"  | _
        "monthly-hot-feeds" | _
    }

    def "Get yearly popular diaries"() {
        expect:
        mockMvc.perform(get("/rank/yearly-hot-diaries"))
                .andDo(document())
                .andExpect(status().isOk())
    }
}
