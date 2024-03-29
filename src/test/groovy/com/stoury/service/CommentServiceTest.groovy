package com.stoury.service

import com.stoury.domain.Comment
import com.stoury.domain.Feed
import com.stoury.domain.Member
import com.stoury.dto.comment.CommentResponse
import com.stoury.exception.authentication.NotAuthorizedException
import com.stoury.exception.comment.CommentCreateException
import com.stoury.exception.comment.CommentSearchException
import com.stoury.repository.CommentRepository
import com.stoury.repository.FeedRepository
import com.stoury.repository.MemberRepository
import spock.lang.Specification

class CommentServiceTest extends Specification {
    def memberRepository = Mock(MemberRepository)
    def feedRepository = Mock(FeedRepository)
    def commentRepository = Mock(CommentRepository)
    def commentService = new CommentService(commentRepository, memberRepository, feedRepository)

    def member = new Member(id:1)
    def feed = new Feed(id:1)
    def savedComment = new Comment(id:1, member: member, feed: feed, textContent: "This is comment")
    def childComment = new Comment(id:2, member: member, parentComment:  savedComment, textContent:  "This is child comment")

    def setup() {
        memberRepository.findById(1L) >> Optional.of(member)
        feedRepository.findById(1L) >> Optional.of(feed)
        commentRepository.findById(1L) >> Optional.of(savedComment)
    }

    def "댓글 생성 성공"() {
        when:
        commentService.createComment(1L, 1L, "This is comment")
        then:
        1 * commentRepository.save(_) >> savedComment
    }

    def "대댓글 생성 성공"() {
        when:
        commentService.createChildComment(1L, 1L, "This is nested comment")
        then:
        1 * commentRepository.save(_) >> childComment
        savedComment.hasChildComments
    }

    def "대대댓글 생성 실패"() {
        when:
        commentService.createChildComment(1L, 2L, "This is double nested comment")
        then:
        commentRepository.findById(2) >> Optional.of(childComment)
        thrown(CommentCreateException.class)
    }

    def "댓글 조회"() {
        when:
        commentService.getCommentsOfFeed(1L, Long.MAX_VALUE)
        then:
        1 * commentRepository.findAllByFeedAndIdLessThanAndParentCommentIsNull(_, _, _) >> List.of(savedComment)
    }

    def "대댓글 조회"() {
        when:
        commentService.getChildComments(1L, Long.MAX_VALUE)
        then:
        1 * commentRepository.findAllByParentCommentAndIdLessThan(_, _, _) >> List.of(childComment)
    }

    def "대댓글 조회 실패 - 대댓글의 대댓글을 조회하려 함"() {
        when:
        commentService.getChildComments(1L, Long.MAX_VALUE)
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

    def "댓글 삭제 실패 - 내가 쓴 거 아님"() {
        when:
        commentService.deleteCommentIfOwner(1L, 2L)
        then:
        thrown(NotAuthorizedException)
    }

    def "댓글 삭제 성공 - 내가 쓴 거임"() {
        when:
        commentService.deleteCommentIfOwner(1, member.getId())
        then:
        savedComment.deleted
    }
}
