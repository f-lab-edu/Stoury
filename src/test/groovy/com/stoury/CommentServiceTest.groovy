package com.stoury

import com.stoury.domain.Comment
import com.stoury.domain.Feed
import com.stoury.domain.Member
import com.stoury.exception.CommentCreateException
import com.stoury.exception.CommentSearchException
import com.stoury.repository.CommentRepository
import com.stoury.repository.FeedRepository
import com.stoury.repository.MemberRepository
import com.stoury.service.CommentService
import spock.lang.Specification

import java.time.LocalDateTime

class CommentServiceTest extends Specification {
    def memberRepository = Mock(MemberRepository)
    def feedRepository = Mock(FeedRepository)
    def commentRepository = Mock(CommentRepository)
    def commentService = new CommentService(commentRepository, memberRepository, feedRepository)

    def member = Mock(Member)
    def feed = Mock(Feed)
    def savedComment = Mock(Comment)
    def savedNestedComment = Mock(Comment)

    def setup() {
        member.getId() >> 1L
        feed.getId() >> 1L
        savedComment.getId() >> 1L
        savedComment.getMember() >> member
        savedComment.getFeed() >> feed
        savedNestedComment.getId() >> 2L
        savedNestedComment.getMember() >> member
        savedNestedComment.getParentComment() >> savedComment
        savedNestedComment.hasParent() >> true
        memberRepository.existsById(_) >> true
        feedRepository.existsById(_) >> true
        commentRepository.existsById(_) >> true
    }

    def "댓글 생성 성공"() {
        when:
        commentService.createComment(member, feed, "This is comment")
        then:
        1 * commentRepository.save(_ as Comment) >> savedComment
    }

    def "대댓글 생성 성공"() {
        when:
        commentService.createNestedComment(member, savedComment, "This is nested comment")
        then:
        1 * commentRepository.save(_ as Comment) >> savedNestedComment
    }

    def "대대댓글 생성 실패"() {
        when:
        commentService.createNestedComment(member, savedNestedComment, "This is double nested comment")
        then:
        thrown(CommentCreateException.class)
    }

    def "댓글 조회"() {
        when:
        commentService.getCommentsOfFeed(feed, LocalDateTime.now())
        then:
        1 * commentRepository.findAllByFeedAndCreatedAtBefore(_, _, _) >> List.of(savedComment)
    }

    def "대댓글 조회"() {
        when:
        commentService.getNestedComments(savedComment, LocalDateTime.now())
        then:
        1 * commentRepository.findAllByParentCommentAndCreatedAtBefore(_, _, _) >> List.of(savedNestedComment)
    }

    def "대댓글 조회 실패 - 대댓글의 대댓글을 조회하려 함"() {
        when:
        commentService.getNestedComments(savedNestedComment, LocalDateTime.now())
        then:
        thrown(CommentSearchException.class)
    }
}
