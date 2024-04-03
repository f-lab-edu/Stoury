package com.stoury.controller

import com.stoury.domain.Comment
import com.stoury.dto.SimpleMemberResponse
import com.stoury.dto.comment.ChildCommentResponse
import com.stoury.dto.comment.CommentResponse
import com.stoury.dto.member.AuthenticatedMember
import com.stoury.service.CommentService
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders

import java.time.LocalDateTime

import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.when
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(CommentController.class)
class CommentControllerTest extends AbstractRestDocsTests {
    @MockBean
    CommentService commentService

    def "Create comment"() {
        given:
        def parameterDescriptor = parameterWithName("feedId").description("id of feed")
        def writer = new AuthenticatedMember(1L, "test@email.com", "pwdpwdpwd123")
        def commentText = "This is comment"
        when(commentService.createComment(any(), any(), any()))
                .thenReturn(new CommentResponse(
                        1L,
                        new SimpleMemberResponse(1L, "writer"),
                        1L,
                        false,
                        commentText,
                        LocalDateTime.of(2024, 12, 31, 13, 30, 05, 101))
                )
        when:
        def response = mockMvc.perform(post("/comments/feed/{feedId}", "1")
                .content(commentText)
                .contentType(MediaType.TEXT_PLAIN)
                .with(authenticatedMember(writer)))
                .andDo(documentWithPath(parameterDescriptor))
        then:
        response.andExpect(status().isOk())
    }

    def "Create child comment"() {
        given:
        def parameterDescriptor = parameterWithName("commentId").description("id of comment")
        def writer = new AuthenticatedMember(1L, "test@email.com", "pwdpwdpwd123")
        def commentText = "This is comment"
        when(commentService.createChildComment(any(), any(), any()))
                .thenReturn(new ChildCommentResponse(
                        1L,
                        new SimpleMemberResponse(1L, "writer"),
                        1L,
                        commentText,
                        LocalDateTime.of(2024, 12, 31, 13, 30, 05, 101))
                )
        when:
        def response = mockMvc.perform(post("/comments/comment/{commentId}", "1")
                .content(commentText)
                .contentType(MediaType.TEXT_PLAIN)
                .with(authenticatedMember(writer)))
                .andDo(documentWithPath(parameterDescriptor))
        then:
        response.andExpect(status().isOk())
    }

    def "Get comments"() {
        given:
        def pathParameterDescriptor = parameterWithName("feedId").description("id of feed")
        def queryParameterDescriptor = parameterWithName("cursorId")
                .description("Results which created order than whose id is cursorId").optional()

        when(commentService.getCommentsOfFeed(any(), any(),)).thenReturn(List.of(
                new CommentResponse(1L, new SimpleMemberResponse(2L, "member2"), 1L, false, "First comment", LocalDateTime.now()),
                new CommentResponse(2L, new SimpleMemberResponse(3L, "member3"), 1L, true, Comment.DELETED_CONTENT_TEXT, LocalDateTime.now()),
                new CommentResponse(3L, new SimpleMemberResponse(2L, "member2"), 1L, true, "Third comment", LocalDateTime.now()),
        ))
        when:
        def response = mockMvc.perform(get("/comments/feed/{feedId}", "1"))
                .andDo(documentWithPathAndQuery(pathParameterDescriptor, queryParameterDescriptor))

        then:
        response.andExpect(status().isOk())
    }

    def "Get child comments"() {
        given:
        def pathParameterDescriptor = parameterWithName("commentId").description("id of comment")
        def queryParameterDescriptor = parameterWithName("cursorId")
                .description("Results which created order than whose id is cursorId").optional()

        when(commentService.getChildComments(any(), any())).thenReturn(List.of(
                new ChildCommentResponse(11L, new SimpleMemberResponse(1L, "member1"), 1L, "First child comment", LocalDateTime.now()),
                new ChildCommentResponse(12L, new SimpleMemberResponse(2L, "member2"), 1L, "Second child comment", LocalDateTime.now()),
                new ChildCommentResponse(13L, new SimpleMemberResponse(3L, "member3"), 1L, Comment.DELETED_CONTENT_TEXT, LocalDateTime.now()),
        ))
        when:
        def response = mockMvc.perform(get("/comments/comment/{commentId}", "1"))
                .andDo(documentWithPathAndQuery(pathParameterDescriptor, queryParameterDescriptor))

        then:
        response.andExpect(status().isOk())
    }

    def "Delete a comment"() {
        given:
        def writer = new AuthenticatedMember(1L, "test@email.com", "pwdpwdpwd123")
        def pathParameterDescriptor = parameterWithName("commentId")
                .description("id of comment. Comments deleted softly")
        when:
        def response = mockMvc.perform(RestDocumentationRequestBuilders.delete("/comments/{commentId}", "1").with(authenticatedMember(writer)))
                .andDo(documentWithPath(pathParameterDescriptor))
        then:
        response.andExpect(status().isOk())
    }
}
