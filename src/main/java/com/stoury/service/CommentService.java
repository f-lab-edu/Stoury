package com.stoury.service;

import com.stoury.domain.ChildComment;
import com.stoury.domain.Comment;
import com.stoury.domain.Feed;
import com.stoury.domain.Member;
import com.stoury.dto.comment.ChildCommentResponse;
import com.stoury.dto.comment.CommentResponse;
import com.stoury.exception.authentication.NotAuthorizedException;
import com.stoury.exception.comment.CommentCreateException;
import com.stoury.exception.comment.CommentSearchException;
import com.stoury.exception.feed.FeedSearchException;
import com.stoury.exception.member.MemberSearchException;
import com.stoury.repository.ChildCommentRepository;
import com.stoury.repository.CommentRepository;
import com.stoury.repository.FeedRepository;
import com.stoury.repository.MemberRepository;
import com.stoury.utils.cachekeys.PageSize;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final ChildCommentRepository childCommentRepository;
    private final MemberRepository memberRepository;
    private final FeedRepository feedRepository;

    @Transactional
    public CommentResponse createComment(Long memberId, Long feedId, String commentText) {
        Member member = memberRepository.findById(Objects.requireNonNull(memberId))
                .orElseThrow(MemberSearchException::new);
        Feed feed = feedRepository.findById(Objects.requireNonNull(feedId))
                .orElseThrow(FeedSearchException::new);

        Comment comment = new Comment(member, feed,
                Objects.requireNonNull(commentText, "Comment text can not be empty."));

        return CommentResponse.from(commentRepository.save(comment));
    }

    @Transactional
    public ChildCommentResponse createChildComment(Long memberId, Long parentCommentId, String commentText) {
        Member member = memberRepository.findById(Objects.requireNonNull(memberId))
                .orElseThrow(MemberSearchException::new);
        Comment parentComment = commentRepository.findById(Objects.requireNonNull(parentCommentId))
                .orElseThrow(CommentSearchException::new);

        ChildComment childComment = new ChildComment(member, parentComment,
                Objects.requireNonNull(commentText, "Comment text can not be empty."));

        ChildComment saveChildComment = childCommentRepository.save(childComment);

        return ChildCommentResponse.from(saveChildComment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsOfFeed(Long feedId, Long offsetId) {
        Feed feed = feedRepository.findById(Objects.requireNonNull(feedId))
                .orElseThrow(FeedSearchException::new);
        Long offsetIdNotNull = Objects.requireNonNull(offsetId);

        Pageable pageable = PageRequest.of(0, PageSize.COMMENT_PAGE_SIZE, Sort.by("createdAt").descending());

        return CommentResponse.from(commentRepository.findAllByFeedAndIdLessThan(feed, offsetIdNotNull, pageable));
    }

    @Transactional(readOnly = true)
    public List<ChildCommentResponse> getChildComments(Long parentCommentId, Long offsetId) {
        Comment parentComment = commentRepository.findById(Objects.requireNonNull(parentCommentId))
                .orElseThrow(CommentSearchException::new);
        Long offsetIdNotNull = Objects.requireNonNull(offsetId);

        Pageable pageable = PageRequest.of(0, PageSize.COMMENT_PAGE_SIZE, Sort.by("createdAt").descending());
        List<ChildComment> childComments = childCommentRepository.findAllByParentComment(parentComment, offsetIdNotNull, pageable);

        return ChildCommentResponse.from(childComments);
    }

    protected CommentResponse deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(Objects.requireNonNull(commentId))
                .orElseThrow(CommentSearchException::new);

        comment.delete();
        return CommentResponse.from(comment);
    }

    @Transactional
    public void deleteCommentIfOwner(Long commentId, Long memberId) {
        Comment comment = commentRepository.findById(Objects.requireNonNull(commentId))
                .orElseThrow(CommentSearchException::new);

        if (comment.notOwnedBy(memberId)) {
            throw new NotAuthorizedException();
        }
        deleteComment(commentId);
    }
}
