package com.stoury.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.stoury.domain.Tag;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static com.stoury.domain.QTag.tag;

@Repository
@RequiredArgsConstructor
public class TagRepository {
    private final JPAQueryFactory queryFactory;
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
        queryFactory.delete(tag).execute();
    }

    public long count() {
        return queryFactory.select(tag.count()).from(tag).fetchFirst();
    }

    public Optional<Tag> findByTagName(String tagName) {
        return Optional.ofNullable(queryFactory
                .selectFrom(tag)
                .where(tag.tagName.eq(tagName))
                .fetchFirst()
        );
    }
}
