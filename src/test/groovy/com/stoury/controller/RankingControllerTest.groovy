package com.stoury.controller

import com.stoury.dto.WriterResponse
import com.stoury.dto.feed.SimpleFeedResponse
import com.stoury.service.RankingService
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean

import java.time.temporal.ChronoUnit

import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.when
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(RankingController.class)
class RankingControllerTest extends AbstractRestDocsTests {
    @MockBean
    RankingService rankingService

    def simpleFeeds = List.of(
            new SimpleFeedResponse(2, new WriterResponse(1, "writer1"), "Tokyo", "Japan"),
            new SimpleFeedResponse(5, new WriterResponse(2, "writer2"), "New York", "United States"),
            new SimpleFeedResponse(10, new WriterResponse(3, "writer3"), "London", "United Kingdom"),
            new SimpleFeedResponse(1, new WriterResponse(2, "writer2"), "Moscow", "Russia"),
            new SimpleFeedResponse(6, new WriterResponse(3, "writer3"), "Seoul", "South Korea"),
            new SimpleFeedResponse(8, new WriterResponse(4, "writer4"), "Shanghai", "China"),
            new SimpleFeedResponse(9, new WriterResponse(6, "writer6"), "Osaka", "Japan"),
            new SimpleFeedResponse(100, new WriterResponse(1, "writer1"), "Taipei", "Taiwan"),
            new SimpleFeedResponse(56, new WriterResponse(2, "writer2"), "Paris", "France"),
            new SimpleFeedResponse(102, new WriterResponse(2, "writer2"), "Hanoi", "Vietnam"),
    )

    def setup() {
        when(rankingService.getHotFeeds(any(ChronoUnit.class)))
                .thenReturn(simpleFeeds)
    }

    def "Get popular domestic spots"() {
        given:
        when(rankingService.getPopularDomesticSpots()).thenReturn(List.of(
                "Seoul-si", "Busan-si", "Incheon-si", "Daegu-si", "Daejeon-si",
                "Gwangju-si", "Suwon-si", "Ulsan-si", "Sejong-si", "Jeju-si"
        ))
        when:
        def response = mockMvc.perform(get("/rank/domestic-spots"))
                .andDo(document())
        then:
        response.andExpect(status().isOk())
    }

    def "Get popular abroad spots"() {
        given:
        when(rankingService.getPopularDomesticSpots()).thenReturn(List.of(
                "United States", "United Kingdom", "Canada", "Australia", "France",
                "Germany", "Italy", "Spain", "Japan", "China"
        ))
        when:
        def response = mockMvc.perform(get("/rank/abroad-spots"))
                .andDo(document())
        then:
        response.andExpect(status().isOk())
    }

    def "Get daily popular feeds"() {
        expect:
        mockMvc.perform(get("/rank/%s".formatted(chronoUnit)))
                .andDo(document())
                .andExpect(status().isOk())
        where:
        chronoUnit          | _
        "daily-hot-feeds"   | _
        "weekly-hot-feeds"  | _
        "monthly-hot-feeds" | _
    }
}
