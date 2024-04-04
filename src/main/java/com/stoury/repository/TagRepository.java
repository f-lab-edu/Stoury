package com.stoury.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stoury.domain.Tag;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.stoury.domain.QTag.tag;

@Repository
@RequiredArgsConstructor
@Transactional
public class TagRepository {
    private final JPAQueryFactory jpaQueryFactory;
    private final EntityManager entityManager;

    public Tag save(Tag saveTag) {
        entityManager.persist(saveTag);
        return saveTag;
    }

    public Tag saveAndFlush(Tag saveTag) {
        entityManager.persist(saveTag);
        entityManager.flush();
        return saveTag;
    }

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
}
