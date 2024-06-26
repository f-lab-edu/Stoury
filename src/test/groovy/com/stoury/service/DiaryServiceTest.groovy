package com.stoury.service

import com.stoury.domain.Diary
import com.stoury.domain.Feed
import com.stoury.domain.GraphicContent
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

    def writer = new Member(id: 1L, email: "writer@email.com", encryptedPassword: "qwdqwdqwd", username: "writer", introduction: null)

    def setup() {
        memberRepository.findById(_) >> Optional.of(writer)
    }

    Feed createFeed(long feedId, LocalDateTime localDateTime, int likes) {
        def feed = new Feed(writer, "feed#" + feedId, 0, 0, [] as Set, "city", "country")
        feed.id = feedId
        feed.createdAt = localDateTime
        feedRepository.findById(feedId) >> Optional.of(feed)
        likeRepository.getLikes(String.valueOf(feedId)) >> likes
        return feed
    }

    def "여행일지 생성 성공"() {
        given:
        def feed1 = createFeed(1L, LocalDateTime.of(2024, 10, 10, 1, 0), 2)
        def feed2 = createFeed(2L, LocalDateTime.of(2024, 10, 10, 2, 0), 2)
        def feed3 = createFeed(3L, LocalDateTime.of(2024, 10, 9, 1, 0), 2)
        def feed4 = createFeed(4L, LocalDateTime.of(2024, 10, 13, 2, 0), 2)

        def thumbnail = new GraphicContent("blabla.jpeg", 0)
        thumbnail.id = 1
        feed3.graphicContents = List.of(thumbnail)

        diaryRepository.save(_) >> new Diary(writer, List.of(feed1, feed2, feed3, feed4), "test diary", thumbnail)

        def diaryRequest = new DiaryCreateRequest("test diary", List.of(1L, 2L, 3L, 4L), thumbnail.id)
        when:
        def diaryResponse = diaryService.createDiary(diaryRequest, 1)
        then:
        diaryResponse.title() == "test diary"
        diaryResponse.feeds().get(1L).size() == 1
        diaryResponse.feeds().get(2L).size() == 2
        diaryResponse.feeds().get(5L).size() == 1
        diaryResponse.likes() == 8
        diaryResponse.thumbnailPath() == thumbnail.path
    }

    def "기본 여행일지 제목(나라이름, 도시이름, yyyy-MM-dd~yyyy-MM-dd) 테스트"() {
        given:
        def feed1 = createFeed(1L, LocalDateTime.of(2024, 10, 10, 1, 0), 2)
        def feed2 = createFeed(2L, LocalDateTime.of(2024, 10, 10, 2, 0), 2)
        def feed3 = createFeed(3L, LocalDateTime.of(2024, 10, 9, 1, 0), 2)
        def feed4 = createFeed(4L, LocalDateTime.of(2024, 10, 13, 2, 0), 2)
        feed3.city = "testCity"
        feed3.country = "testCountry"

        expect:
        diaryService.getDefaultTitle(List.of(feed3, feed1, feed2, feed4)) ==
                feed3.country + ", " + feed3.city + ", " + feed3.createdAt.toLocalDate() + "~" + feed4.createdAt.toLocalDate()
    }

    def "사용자의 여행일지 조회"() {
        when:
        diaryService.getMemberDiaries(1L, Long.MAX_VALUE)
        then:
        1 * diaryRepository.findByMemberAndIdLessThan(_ as Member, _, _) >> []
    }

    def "단일 여행일지 조회"() {
        given:
        def member = new Member(id: 3)
        Diary diary = new Diary(
                id: 1,
                feeds: [
                        new Feed(id: 1, member: member, createdAt: LocalDateTime.of(2024, 1, 1, 13, 0)),
                        new Feed(id: 2, member: member, createdAt: LocalDateTime.of(2024, 1, 2, 13, 0)),
                        new Feed(id: 3, member: member, createdAt: LocalDateTime.of(2024, 1, 4, 13, 0))
                ],
                title: "Test stoury",
                thumbnail: new GraphicContent("/path", 1),
                member: member
        )
        diaryRepository.findById(1) >> Optional.of(diary)
        likeRepository.getLikes("1") >> 3
        likeRepository.getLikes("2") >> 4
        likeRepository.getLikes("3") >> 5
        when:
        def diaryResponse = diaryService.getDiary(1)
        then:
        diaryResponse.id() == 1
        diaryResponse.title() == "Test stoury"
        diaryResponse.feeds().size() == 3
        diaryResponse.memberId() == 3
        diaryResponse.likes() == 12
    }
}
