package com.stoury.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stoury.domain.Feed;
import com.stoury.domain.Member;
import com.stoury.domain.RecommendFeed;
import com.stoury.domain.Tag;
import com.stoury.projection.FeedResponseEntity;
import com.stoury.utils.PageSize;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.stoury.domain.QClickLog.clickLog;
import static com.stoury.domain.QFeed.feed;
import static com.stoury.domain.QFollow.follow;
import static com.stoury.domain.QGraphicContent.graphicContent;
import static com.stoury.domain.QRecommendFeed.recommendFeed;
import static com.stoury.domain.QTag.tag;
import static com.stoury.projection.QFeedResponseEntity.feedResponseEntity;

@Repository
@RequiredArgsConstructor
public class FeedRepository {
    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;

    @Transactional
    public Feed save(Feed saveFeed) {
        entityManager.persist(saveFeed);
        entityManager.refresh(saveFeed);
        return saveFeed;
    }

    @Transactional
    public FeedResponseEntity saveFeedResponse(FeedResponseEntity saveFeedResponseEntity) {
        entityManager.persist(saveFeedResponseEntity);
        return saveFeedResponseEntity;
    }

    @Transactional
    public Optional<FeedResponseEntity> findFeedResponseById(Long feedId) {
        return Optional.ofNullable(jpaQueryFactory
                .selectFrom(feedResponseEntity)
                .where(feedResponseEntity.feedId.eq(feedId))
                .fetchFirst());
    }

    @Transactional
    public void delete(Feed deleteFeed) {
        entityManager.remove(deleteFeed);
    }

    @Transactional(readOnly = true)
    public Optional<Feed> findById(Long id) {
        return Optional.ofNullable(jpaQueryFactory
                .selectFrom(feed)
                .where(feed.id.eq(id))
                .fetchFirst());
    }

    @Transactional(readOnly = true)
    public List<FeedResponseEntity> findAllFeedsByMemberAndIdLessThan(Member feedWriter, Long offsetId, Pageable page){
        return jpaQueryFactory
                .selectFrom(feedResponseEntity)
                .where(feedResponseEntity.feedId.lt(offsetId)
                        .and(feedResponseEntity.writerId.eq(feedWriter.getId())))
                .orderBy(feedResponseEntity.feedId.desc())
                .offset(page.getOffset())
                .limit(page.getPageSize())
                .fetch();
    }

    @Transactional(readOnly = true)
    public List<FeedResponseEntity> findByTagNameAndIdLessThan(String tagName, Long offsetId, Pageable page) {
        List<Long> feedIds = findAllFeedIdByTagAndIdLessThan(tagName, offsetId, page);

        return jpaQueryFactory
                .selectFrom(feedResponseEntity)
                .where(feedResponseEntity.feedId.in(feedIds))
                .orderBy(feedResponseEntity.feedId.desc())
                .fetch();
    }

    @Transactional(readOnly = true)
    public List<FeedResponseEntity> findAllFeedsByIdIn(Collection<Long> feedIds) {
        return jpaQueryFactory
                .selectFrom(feedResponseEntity)
                .where(feedResponseEntity.feedId.in(feedIds))
                .fetch();
    }

    private List<Long> findAllFeedIdByTagAndIdLessThan(String tagName, Long offsetId, Pageable page) {
        return jpaQueryFactory
                .select(feed.id)
                .from(feed)
                .innerJoin(feed.tags, tag).on(tag.tagName.eq(tagName))
                .where(feed.id.lt(offsetId))
                .orderBy(feed.id.desc())
                .offset(page.getOffset())
                .limit(page.getPageSize())
                .fetch();
    }

    @Transactional(readOnly = true)
    public List<String> findTop10CitiesInKorea(Pageable pageable) {
        return findTop10(feed.city, feed.country.eq("South Korea"), pageable);
    }

    @Transactional(readOnly = true)
    public List<String> findTop10CountriesNotKorea(Pageable pageable) {
        return findTop10(feed.country, feed.country.ne("South Korea"), pageable);
    }

    @Transactional(readOnly = true)
    public List<String> findTop10(StringPath column, BooleanExpression expression, Pageable pageable) {
        return jpaQueryFactory
                .select(column)
                .from(feed)
                .where(expression)
                .groupBy(column)
                .having(column.ne("UNDEFINED"))
                .orderBy(feed.count().desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Transactional
    public void deleteAll() {
        jpaQueryFactory.delete(graphicContent).execute();
        jpaQueryFactory.delete(feed).execute();
    }

    @Transactional
    public void deleteAllFeedResponse() {
        jpaQueryFactory.delete(feedResponseEntity).execute();
    }

    @Transactional
    public Feed saveAndFlush(Feed saveFeed) {
        entityManager.persist(saveFeed);
        return saveFeed;
    }

    @Transactional
    public List<Feed> saveAll(Collection<Feed> feeds) {
        return feeds.stream().map(this::save).toList();
    }

    @Transactional
    public List<FeedResponseEntity> saveAllFeedResponses(Collection<FeedResponseEntity> feeds) {
        return feeds.stream().map(this::saveFeedResponse).toList();
    }

    @Transactional
    public void deleteFeedResponseById(Long feedId) {
        jpaQueryFactory.delete(feedResponseEntity)
                .where(feedResponseEntity.feedId.eq(feedId))
                .execute();
    }

    @Transactional(readOnly = true)
    public List<Long> findRandomFeedIdsByTagName(Collection<Tag> tagNames){
        return jpaQueryFactory.
                select(feed.id)
                .from(feed)
                .join(feed.tags, tag)
                .where(tag.in(tagNames))
                .orderBy(Expressions.numberTemplate(Double.class, "function('rand')").asc())
                .limit(PageSize.RANDOM_FEEDS_FETCH_SIZE)
                .fetch();
    }

    @Transactional
    public List<RecommendFeed> saveRecommendFeeds(Collection<RecommendFeed> recommendFeeds) {
        recommendFeeds.forEach(entityManager::persist);
        return recommendFeeds.stream().toList();
    }

    @Transactional(readOnly = true)
    public List<Long> findAllRecommendFeedIds(Long memberId) {
        return jpaQueryFactory
                .select(recommendFeed.feedId)
                .from(recommendFeed)
                .where(recommendFeed.memberId.eq(memberId))
                .fetch();
    }

    @Transactional(readOnly = true)
    public List<Long> findRandomRecommendFeedIds(Long memberId) {
        return jpaQueryFactory
                .select(recommendFeed.feedId)
                .from(recommendFeed)
                .where(recommendFeed.memberId.eq(memberId))
                .orderBy(Expressions.numberTemplate(Double.class, "function('rand')").asc())
                .limit(PageSize.RANDOM_FEEDS_FETCH_SIZE)
                .fetch();
    }

    @Transactional(readOnly = true)
    public List<Long> findFollowerViewedFeedsOfMember(Long memberId) {
        LocalDateTime current = LocalDateTime.now();
        LocalDateTime aWeekAgo = current.minusDays(7);

        return jpaQueryFactory
                .select(clickLog.feedId)
                .from(clickLog).join(follow).on(clickLog.memberId.eq(follow.follower.id))
                .where(follow.followee.id.eq(memberId)
                        .and(clickLog.createdAt.between(aWeekAgo, current)))
                .fetch();
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long feedId) {
        return !notExistsById(feedId);
    }

    @Transactional(readOnly = true)
    public boolean notExistsById(Long feedId) {
        return findById(feedId).isEmpty();
    }
}
