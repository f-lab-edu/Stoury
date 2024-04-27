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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
@ConditionalOnExpression("'${spring.batch.job.names}'.contains('jobFollowersRecommendFeeds')")
public class BatchFollowersRecommendFeedsConfig {
    private final LogJobExecutionListener logger;
    private final EntityManagerFactory entityManagerFactory;
    private final FeedRepository feedRepository;
    private final FollowRepository followRepository;

    @Bean
    public Job updateFollowersRecommendFeedsJob(JobRepository jobRepository, PlatformTransactionManager tm,
                                                ThreadPoolTaskExecutor taskExecutor) {
        return new JobBuilder("jobFollowersRecommendFeeds", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(updateFollowersRecommendFeedsStep(jobRepository, tm, taskExecutor))
                .listener(logger)
                .build();
    }

    @Bean
    public Step updateFollowersRecommendFeedsStep(JobRepository jobRepository, PlatformTransactionManager tm, ThreadPoolTaskExecutor taskExecutor) {
        return new StepBuilder("stepFollowersRecommendFeeds", jobRepository)
                .<Long, MemberRecommendFeedIds>chunk(1000, tm)
                .reader(memberReader())
                .processor(followViewedFeedsProcessor())
                .writer(recommendFeedsWriter())
                .taskExecutor(taskExecutor)
                .allowStartIfComplete(true)
                .build();
    }

    public JpaPagingItemReader<Long> memberReader() {
        return new JpaPagingItemReaderBuilder<Long>()
                .name("memberReader")
                .pageSize(1000)
                .entityManagerFactory(entityManagerFactory)
                .queryString("select m.id from Member m")
                .build();
    }

    public ItemProcessor<Long, MemberRecommendFeedIds> followViewedFeedsProcessor() {
        return memberId -> {
            Set<Long> recommendFeedIds = followRepository.findFollowerId(memberId).stream()
                    .flatMap(follower -> feedRepository.findViewedFeedIdsByMember(follower).stream())
                    .collect(Collectors.toSet());

            return new MemberRecommendFeedIds(memberId, recommendFeedIds);
        };
    }

    public ItemWriter<MemberRecommendFeedIds> recommendFeedsWriter(){
        return recommendFeedIds -> {
            List<RecommendFeed> recommendFeeds = recommendFeedIds.getItems().stream()
                    .flatMap(rf -> rf.feedIds().stream()
                            .map(feedId -> new RecommendFeed(rf.memberId(), feedId, LocalDateTime.now())))
                    .toList();
            feedRepository.saveRecommendFeeds(recommendFeeds);
        };
    }
}
