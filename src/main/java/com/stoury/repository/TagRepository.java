package com.stoury.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stoury.domain.Tag;
import com.stoury.utils.PageSize;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.stoury.domain.QClickLog.clickLog;
import static com.stoury.domain.QFeed.feed;
import static com.stoury.domain.QTag.tag;

@Repository
@RequiredArgsConstructor
public class TagRepository {
    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;

    @Transactional
    public Tag save(Tag saveTag) {
        entityManager.persist(saveTag);
        return saveTag;
    }

    @Transactional
    public List<Tag> saveAll(Collection<Tag> saveTags) {
        saveTags.forEach(entityManager::persist);
        return saveTags.stream().toList();
    }

    @Transactional
    public Tag saveAndFlush(Tag saveTag) {
        entityManager.persist(saveTag);
        entityManager.flush();
        return saveTag;
    }

    @Transactional
    public void deleteAll() {
        jpaQueryFactory.selectFrom(tag).fetch().forEach(entityManager::remove);
    }

    @Transactional(readOnly = true)
    public long count() {
        return jpaQueryFactory.select(tag.count()).from(tag).fetchFirst();
    }

    @Transactional(readOnly = true)
    public Optional<Tag> findByTagName(String tagName) {
        return Optional.ofNullable(jpaQueryFactory
                .selectFrom(tag)
                .where(tag.tagName.eq(tagName))
                .fetchFirst()
        );
    }

    public List<Tag> findAllByMemberIdAndFrequency(Long memberId) {
        return jpaQueryFactory
                .select(tag)
                .from(clickLog)
                .join(feed).on(clickLog.feedId.eq(feed.id))
                .join(feed.tags, tag)
                .where(clickLog.memberId.eq(memberId)
                        .and(clickLog.createdAt.between(LocalDateTime.now().minusDays(7), LocalDateTime.now())))
                .groupBy(tag)
                .orderBy(tag.count().desc())
                .limit(PageSize.FREQUENT_TAGS_SIZE)
                .fetch();
    }
}
