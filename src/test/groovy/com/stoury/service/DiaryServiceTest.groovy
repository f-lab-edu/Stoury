package com.stoury.service

import com.stoury.domain.Diary
import com.stoury.domain.Feed
import com.stoury.domain.Member
import com.stoury.dto.diary.DiaryCreateRequest
import com.stoury.repository.DiaryRepository
import com.stoury.repository.FeedRepository
import com.stoury.repository.LikeRepository
import com.stoury.repository.MemberRepository
import spock.lang.Specification

import java.time.LocalDateTime

class DiaryServiceTest extends Specification {
    def memberRepository = Mock(MemberRepository)
    def feedRepository = Mock(FeedRepository)
    def diaryRepository = Mock(DiaryRepository)
    def likeRepository = Mock(LikeRepository)
    def diaryService = new DiaryService(memberRepository, feedRepository, diaryRepository, likeRepository)

    def writer = new Member("writer@email.com", "qwdqwdqwd", "writer", null)

    def setup() {
        memberRepository.findById(_) >> Optional.of(writer)
    }

    def "여행일지 생성 성공"() {
        given:
        def feed1 = new Feed(writer, "feed#1", 0, 0, Collections.emptyList())
        feed1.id = 1L
        feed1.createdAt = LocalDateTime.of(2024, 10, 10, 1, 0)
        likeRepository.getLikes("1") >> 2
        feedRepository.findById(1) >> Optional.of(feed1)

        def feed2 = new Feed(writer, "feed#2", 0, 0, Collections.emptyList())
        feed2.id = 2L
        feed2.createdAt = LocalDateTime.of(2024, 10, 10, 2, 0)
        likeRepository.getLikes("2") >> 2
        feedRepository.findById(2) >> Optional.of(feed2)

        def feed3 = new Feed(writer, "feed#3", 0, 0, Collections.emptyList())
        feed3.id = 3L
        feed3.createdAt = LocalDateTime.of(2024, 10, 9, 1, 0)
        likeRepository.getLikes("3") >> 2
        feedRepository.findById(3) >> Optional.of(feed3)

        def feed4 = new Feed(writer, "feed#4", 0, 0, Collections.emptyList())
        feed4.id = 4L
        feed4.createdAt = LocalDateTime.of(2024, 10, 13, 2, 0)
        likeRepository.getLikes("4") >> 2
        feedRepository.findById(4) >> Optional.of(feed4)

        diaryRepository.save(_) >> new Diary(writer, List.of(feed1, feed2, feed3, feed4), "test diary")

        def diaryRequest = new DiaryCreateRequest("test diary", List.of(1L, 2L, 3L, 4L))
        when:
        def diaryResponse = diaryService.createDiary(diaryRequest, 1)
        then:
        diaryResponse.title() == "test diary"
        diaryResponse.feeds().get(1L).size() == 1
        diaryResponse.feeds().get(2L).size() == 2
        diaryResponse.feeds().get(5L).size() == 1
        diaryResponse.likes() == 8
    }

    def "기본 여행일지 제목(나라이름, 도시이름, yyyy-MM-dd~yyyy-MM-dd) 테스트"() {
        given:
        def feed1 = new Feed(writer, "feed#1", 0, 0, Collections.emptyList())
        feed1.createdAt = LocalDateTime.of(2024, 10, 10, 1, 0)
        def feed2 = new Feed(writer, "feed#2", 0, 0, Collections.emptyList())
        feed2.createdAt = LocalDateTime.of(2024, 10, 10, 2, 0)
        def feed3 = new Feed(writer, "feed#3", 0, 0, Collections.emptyList())
        feed3.createdAt = LocalDateTime.of(2024, 10, 9, 1, 0)
        feed3.city = "testCity"
        feed3.country = "testCountry"
        def feed4 = new Feed(writer, "feed#4", 0, 0, Collections.emptyList())
        feed4.createdAt = LocalDateTime.of(2024, 10, 13, 2, 0)

        expect:
        diaryService.getDefaultTitle(List.of(feed3,feed1,feed2,feed4)) ==
                feed3.country + ", " + feed3.city + ", " + feed3.createdAt.toLocalDate() + "~" + feed4.createdAt.toLocalDate()
    }
}
