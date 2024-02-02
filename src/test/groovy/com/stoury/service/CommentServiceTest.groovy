package com.stoury.service

import com.stoury.domain.Comment
import com.stoury.domain.Feed
import com.stoury.domain.Member
import com.stoury.dto.comment.CommentResponse
import com.stoury.exception.CommentCreateException
import com.stoury.exception.CommentSearchException
import com.stoury.repository.CommentRepository
import com.stoury.repository.FeedRepository
import com.stoury.repository.MemberRepository
import spock.lang.Specification

import java.time.LocalDateTime

class CommentServiceTest extends Specification {
    def memberRepository = Mock(MemberRepository)
    def feedRepository = Mock(FeedRepository)
    def commentRepository = Mock(CommentRepository)
    def commentService = new CommentService(commentRepository, memberRepository, feedRepository)

    def member = Mock(Member)
    def feed = Mock(Feed)
    def savedComment = new Comment(member, feed, "This is comment")
    def childComment = new Comment(member, savedComment, "This is child comment")

    def setup() {
        memberRepository.findById(_ as Long) >> Optional.of(member)
        feedRepository.findById(_ as Long) >> Optional.of(feed)
        commentRepository.findById(_ as Long) >> Optional.of(savedComment)
    }

    def "댓글 생성 성공"() {
        when:
        commentService.createComment(1L, 2L, "This is comment")
        then:
        1 * commentRepository.save(_ as Comment) >> savedComment
    }

    def "대댓글 생성 성공"() {
        when:
        commentService.createNestedComment(1L, 2L, "This is nested comment")
        then:
        1 * commentRepository.save(_ as Comment) >> childComment
    }

    def "대대댓글 생성 실패"() {
        when:
        commentService.createNestedComment(1L, 2L, "This is double nested comment")
        then:
        commentRepository.findById(_ as Long) >> Optional.of(childComment)
        thrown(CommentCreateException.class)
    }

    def "댓글 조회"() {
        when:
        commentService.getCommentsOfFeed(1L, LocalDateTime.now())
        then:
        1 * commentRepository.findAllByFeedAndCreatedAtBefore(_, _, _) >> List.of(savedComment)
    }

    def "대댓글 조회"() {
        when:
        commentService.getChildComments(1L, LocalDateTime.now())
        then:
        1 * commentRepository.findAllByParentCommentAndCreatedAtBefore(_, _, _) >> List.of(childComment)
    }

    def "대댓글 조회 실패 - 대댓글의 대댓글을 조회하려 함"() {
        when:
        commentService.getChildComments(1L, LocalDateTime.now())
        then:
        commentRepository.findById(1L) >> Optional.of(childComment)
        thrown(CommentSearchException.class)
    }

    def "댓글 삭제 성공"() {
        given:
        def comment = new Comment(member, feed, "blabla")
        when:
        commentService.deleteComment(1L)
        then:
        commentRepository.findById(1L) >> Optional.of(comment)
        comment.isDeleted()
        CommentResponse.from(comment).textContent() == "This comment was deleted"
    }
}
