package com.stoury

import com.stoury.domain.*
import com.stoury.dto.SimpleMemberResponse
import com.stoury.dto.feed.FeedCreateRequest
import com.stoury.dto.feed.SimpleFeedResponse
import com.stoury.dto.member.AuthenticatedMember
import com.stoury.dto.member.MemberResponse
import com.stoury.repository.*
import com.stoury.service.FeedService
import com.stoury.service.MemberService
import com.stoury.utils.cachekeys.PageSize
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Slice
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.http.MediaType
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.mock.web.MockMultipartFile
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import org.springframework.test.annotation.Rollback
import org.springframework.test.context.ActiveProfiles
import spock.lang.Specification

import java.time.temporal.ChronoUnit
import java.util.stream.IntStream

import static com.stoury.utils.cachekeys.HotFeedsKeys.getHotFeedsKey

@SpringBootTest
@ActiveProfiles("test")
class IntegrationTest extends Specification {
    @Autowired
    TagRepository tagRepository
    @Autowired
    FeedRepository feedRepository
    @Autowired
    MemberRepository memberRepository
    @Autowired
    DiaryRepository diaryRepository
    @Autowired
    LikeRepository likeRepository
    @Autowired
    RankingRepository rankingRepository
    @Autowired
    MemberOnlineStatusRepository memberOnlineStatusRepository
    @Autowired
    StringRedisTemplate redisTemplate
    @PersistenceContext
    EntityManager entityManager
    @Autowired
    MemberService memberService
    @Autowired
    FeedService feedService
    @Autowired
    AuthenticationSuccessHandler authenticationSuccessHandler
    @Autowired
    LogoutSuccessHandler logoutSuccessHandler

    def member = new Member("aaa@dddd.com", "qwdqwdqwd", "username", null)

    def setup() {
        feedRepository.deleteAll()
        diaryRepository.deleteAll()
        memberRepository.deleteAll()
        tagRepository.deleteAll()
        memberRepository.save(member)

        Set<String> allKeys = redisTemplate.keys("*")
        redisTemplate.delete(allKeys)
    }
    
    def cleanup() {
        feedRepository.deleteAll()
        diaryRepository.deleteAll()
        memberRepository.deleteAll()
        tagRepository.deleteAll()

        Set<String> allKeys = redisTemplate.keys("*")
        redisTemplate.delete(allKeys)
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
        def writer = new SimpleMemberResponse(1L, "writer")
        given:
        def feeds = [
                new SimpleFeedResponse(0L, writer, "city0", "country0"),
                new SimpleFeedResponse(1L, writer, "city1", "country1"),
                new SimpleFeedResponse(2L, writer, "city2", "country2"),
                new SimpleFeedResponse(3L, writer, "city3", "country3"),
                new SimpleFeedResponse(4L, writer, "city4", "country4"),
                new SimpleFeedResponse(5L, writer, "city5", "country5"),
                new SimpleFeedResponse(6L, writer, "city6", "country6"),
                new SimpleFeedResponse(7L, writer, "city7", "country7"),
                new SimpleFeedResponse(8L, writer, "city8", "country8"),
                new SimpleFeedResponse(9L, writer, "city9", "country9"),
                new SimpleFeedResponse(10L, writer, "city10", "country10"),
                new SimpleFeedResponse(11L, writer, "city11", "country11"),
                new SimpleFeedResponse(12L, writer, "city12", "country12"),
                new SimpleFeedResponse(13L, writer, "city13", "country13"),
                new SimpleFeedResponse(14L, writer, "city14", "country14"),
                new SimpleFeedResponse(15L, writer, "city15", "country15"),
                new SimpleFeedResponse(16L, writer, "city16", "country16"),
                new SimpleFeedResponse(17L, writer, "city17", "country17"),
                new SimpleFeedResponse(18L, writer, "city18", "country18"),
                new SimpleFeedResponse(19L, writer, "city19", "country19"),
        ]

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
                .forEach(i -> rankingRepository.saveHotFeed(
                        feeds.get(i),
                        likeIncreases.get(i),
                        ChronoUnit.DAYS))

        then:
        def rankedList = rankingRepository.getRankedFeeds(getHotFeedsKey(ChronoUnit.DAYS)).stream()
                .map(SimpleFeedResponse::id).toList()
        List<Long> expectedList = List.of(
                15L, 5L, 14L, 3L, 12L,
                18L, 17L, 13L, 6L, 16L)
        rankedList == expectedList
    }

    @Rollback(false)
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
        memberRepository.saveAllAndFlush([member1, member2, member3, member4, member5, member6, member7, member8])

        when:
        Slice<MemberResponse> slice = memberService.searchMembers("mem")
        List<MemberResponse> foundMembers = slice.getContent();

        then:
        slice.size == PageSize.MEMBER_PAGE_SIZE
        slice.hasNext()
        foundMembers.get(0).username() == member1.getUsername()
        foundMembers.get(1).username() == member2.getUsername()
        foundMembers.get(2).username() == member5.getUsername()
        foundMembers.get(3).username() == member6.getUsername()
        foundMembers.get(4).username() == member7.getUsername()
    }

    def "로그인 성공시 online상태여야 함"() {
        given:
        def request = new MockHttpServletRequest()
        request.setMethod("POST")
        request.setContentType(MediaType.MULTIPART_FORM_DATA.toString())
        request.setParameter("latitude", "37.123123")
        request.setParameter("longitude", "127.123123")
        def response = new MockHttpServletResponse()
        def authentication = Mock(Authentication)
        authentication.getPrincipal() >> new AuthenticatedMember(1, "test@email.com", "pwdpwdpwd123")
        when:
        authenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication)
        then:
        redisTemplate.opsForSet().isMember(MemberOnlineStatusRepository.ONLINE_MEMBER_CACHE_KEY, "1")
    }

    def "로그아웃 성공시 offline상태여야 함"() {
        given:
        redisTemplate.opsForSet().add(MemberOnlineStatusRepository.ONLINE_MEMBER_CACHE_KEY, "1")
        def request = new MockHttpServletRequest()
        request.setMethod("POST")
        def response = new MockHttpServletResponse()
        def authentication = Mock(Authentication)
        authentication.getPrincipal() >> new AuthenticatedMember(1, "test@email.com", "pwdpwdpwd123")
        when:
        logoutSuccessHandler.onLogoutSuccess(request, response, authentication)
        then:
        !redisTemplate.opsForSet().isMember(MemberOnlineStatusRepository.ONLINE_MEMBER_CACHE_KEY, "1")
    }

    def "피드 생성 시 이미지 같이 생성돼야함"() {
        given:
        def feed = Feed.builder()
                .member(member)
                .textContent("Feed with images")
                .latitude(36.125).longitude(127.125)
                .city("city").country("country")
                .build()
        feed.addGraphicContents(List.of(
                new GraphicContent("path1", 0),
                new GraphicContent("path2", 1),
                new GraphicContent("path3", 2),
        ))
        when:
        def savedFeed = feedRepository.save(feed)
        then:
        savedFeed.graphicContents.size() == 3
    }

    def "피드생성시 태그같이 생성되거나 기존 태그 사용"() {
        given:
        tagRepository.saveAndFlush(new Tag("tag0"))
        def feedCreateRequest = FeedCreateRequest.builder()
                .textContent("Feed with tags")
                .tagNames(["tag0", "tag1", "tag2"] as Set)
                .latitude(0)
                .longitude(0)
                .build()
        def graphicContents = List.of(new MockMultipartFile("images", "image1.jpeg", "image/jpeg", new byte[0]))
        when:
        feedService.createFeed(member.getId(), feedCreateRequest, graphicContents)
        then:
        tagRepository.count() == 3
    }

    def "여행일지 생성&삭제, 삭제해도 기존 피드는 남음"() {
        given:
        def feed = Feed.builder()
                .member(member)
                .textContent("Feed with images")
                .latitude(36.125).longitude(127.125)
                .city("city").country("country")
                .build()
        feed.addGraphicContents(List.of(
                new GraphicContent("path1", 0),
                new GraphicContent("path2", 1),
                new GraphicContent("path3", 2),
        ))
        def savedFeed = feedRepository.saveAndFlush(feed)
        def feedId = savedFeed.getId()
        def diary = new Diary(member, [savedFeed], "test diary", savedFeed.graphicContents.get(0))
        def savedDiary = diaryRepository.saveAndFlush(diary)
        when:
        diaryRepository.delete(savedDiary)
        then:
        feedRepository.existsById(feedId)
        entityManager.createQuery("""
            SELECT G 
            FROM Feed F JOIN FETCH GraphicContent G
            ON F = G.feed 
            WHERE F.id = :id"""
        )
        .setParameter("id", feedId)
        .getResultList()
        .size() == 3
    }

    def "주변 사용자 레디스에서 검색"() {
        given:
        def members = [
                new Member("test1@email.com", "encrypted", "member1", null),
                new Member("test2@email.com", "encrypted", "member2", null),
                new Member("test3@email.com", "encrypted", "member3", null),
                new Member("test4@email.com", "encrypted", "member4", null)
        ]
        def savedMembers = memberRepository.saveAll(members)
        memberOnlineStatusRepository.save(savedMembers.get(0).id, 37.566535, 126.97796919999996)  // 첫번째 멤버로부터
        memberOnlineStatusRepository.save(savedMembers.get(1).id, 37.4562551, 126.70520693063735) // 27km
        memberOnlineStatusRepository.save(savedMembers.get(2).id, 36.3504119, 127.38454750000005) // 139km
        memberOnlineStatusRepository.save(savedMembers.get(3).id, 35.1795543, 129.07564160000004) // 324km
        when:
        def aroundMembers = memberService.searchOnlineMembers(savedMembers.get(0).id, 37.566535, 126.97796919999996, 200)
        def member2 = aroundMembers.get(0)
        def member3 = aroundMembers.get(1)
        then:
        member2.username() == "member2"
        member3.username() == "member3"
        // 거리 오차는 +- 1km 이내여야함
        26 <= member2.distance() && member2.distance() <= 28
        138 <= member3.distance() && member3.distance() <= 140
    }
}
