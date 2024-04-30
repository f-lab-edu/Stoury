package com.stoury.batch;

import com.stoury.domain.RecommendFeed;
import com.stoury.dto.MemberRecommendFeedIds;
import com.stoury.repository.FeedRepository;
import com.stoury.repository.FollowRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@ConditionalOnExpression("'${spring.batch.job.names}'.contains('jobFollowersRecommendFeeds')")
public class BatchFollowersRecommendFeedsConfig {
    private static final int CHUNK_SIZE = 1000;
    private final LogJobExecutionListener logger;
    private final EntityManagerFactory entityManagerFactory;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ThreadPoolTaskExecutor taskExecutor;
    private final FeedRepository feedRepository;
    private final FollowRepository followRepository;

    @Bean
    public Job updateFollowersRecommendFeedsJob() {
        return new JobBuilder("jobFollowersRecommendFeeds", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(updateFollowersRecommendFeedsStep())
                .listener(logger)
                .build();
    }

    @Bean
    public Step updateFollowersRecommendFeedsStep() {
        return new StepBuilder("stepFollowersRecommendFeeds", jobRepository)
                .<Long, MemberRecommendFeedIds>chunk(CHUNK_SIZE, transactionManager)
                .reader(memberReader())
                .processor(followerViewedFeedsAggregator())
                .writer(recommendFeedsWriter())
                .taskExecutor(taskExecutor)
                .allowStartIfComplete(true)
                .build();
    }

    public JpaPagingItemReader<Long> memberReader() {
        return new JpaPagingItemReaderBuilder<Long>()
                .name("memberReader")
                .pageSize(CHUNK_SIZE)
                .entityManagerFactory(entityManagerFactory)
                .queryString("select m.id from Member m")
                .build();
    }

    public ItemProcessor<Long, MemberRecommendFeedIds> followerViewedFeedsAggregator() {
        return memberId -> {
            List<Long> followerViewedFeedsOfMember = feedRepository.findFollowerViewedFeedsOfMember(memberId);
            return MemberRecommendFeedIds.of(memberId, followerViewedFeedsOfMember);
        };
    }

    public ItemWriter<MemberRecommendFeedIds> recommendFeedsWriter() {
        return memberRecommendFeedIds -> {
            List<RecommendFeed> recommendFeeds = memberRecommendFeedIds.getItems().stream()
                    .flatMap(MemberRecommendFeedIds::feedIdsToRecommendFeeds)
                    .toList();
            feedRepository.saveRecommendFeeds(recommendFeeds);
        };
    }
}
