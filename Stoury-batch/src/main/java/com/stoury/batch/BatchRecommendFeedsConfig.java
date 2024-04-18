package com.stoury.batch;

import com.fasterxml.jackson.core.type.TypeReference;
import com.stoury.dto.FrequentTags;
import com.stoury.dto.RecommendFeedIds;
import com.stoury.repository.FeedRepository;
import com.stoury.repository.RecommendFeedsRepository;
import com.stoury.utils.JsonMapper;
import com.stoury.utils.cachekeys.FrequentTagsKey;
import com.stoury.utils.cachekeys.PageSize;
import com.stoury.utils.cachekeys.RecommendFeedsKey;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class BatchRecommendFeedsConfig {
    private final RedisTemplate redisTemplate;
    private final EntityManagerFactory entityManagerFactory;
    private final FeedRepository feedRepository;
    private final RecommendFeedsRepository recommendFeedsRepository;
    private final JsonMapper jsonMapper;

    @Bean
    public Job updateRecommendFeedsJob(JobRepository jobRepository, PlatformTransactionManager tm) {
        return new JobBuilder("jobRecommendFeeds", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(getFrequentTagsStep(jobRepository, tm))
                .next(getRecommendFeedsStep(jobRepository, tm))
                .build();
    }

    @Bean
    public Step getRecommendFeedsStep(JobRepository jobRepository, PlatformTransactionManager tm) {
        return new StepBuilder("stepRecommendFeeds", jobRepository)
                .<FrequentTags, RecommendFeedIds>chunk(1000, tm)
                .reader(new FrequentTagsScanReader(redisTemplate))
                .processor(randomFeedsOfTagProcessor())
                .writer(recommendFeedsWriter())
                .build();
    }

    @Bean
    public Step getFrequentTagsStep(JobRepository jobRepository, PlatformTransactionManager tm) {
        return new StepBuilder("stepFrequentTags", jobRepository)
                .<Long, FrequentTags>chunk(1000, tm)
                .reader(memberIdReader())
                .processor(viewedTagsProcessor())
                .writer(viewedTagsWriter())
                .build();
    }

    private ItemProcessor<FrequentTags, RecommendFeedIds> randomFeedsOfTagProcessor() {
        return frequentTags -> {
            Long memberId = frequentTags.memberId();
            List<String> tagNames = frequentTags.viewedTags();

            List<Long> randomlyRecommendedFeedIds = feedRepository.findRandomFeedIdsByTagName(tagNames);
            return new RecommendFeedIds(memberId, randomlyRecommendedFeedIds);
        };
    }

    private ItemWriter<RecommendFeedIds> recommendFeedsWriter() {
        return recommendFeedIds -> {
            for (RecommendFeedIds recommended : recommendFeedIds) {
                String memberId = recommended.memberId().toString();
                List<Long> feedIds = recommended.feedIds();
                String key = RecommendFeedsKey.getRecommendFeedsKey(memberId);

                redisTemplate.delete(memberId);
                redisTemplate.opsForSet().add(key, feedIds.toString());
            }
        };
    }

    private ItemWriter<FrequentTags> viewedTagsWriter() {
        return viewedTags -> {
            for (FrequentTags viewedTag : viewedTags) {
                String memberId = viewedTag.memberId().toString();
                String key = FrequentTagsKey.getFrequentTagsKey(memberId);

                viewedTag.viewedTags().forEach(tag -> redisTemplate.opsForSet().add(key, tag));
            }
        };
    }

    private JpaPagingItemReader<Long> memberIdReader() {
        return new JpaPagingItemReaderBuilder<Long>()
                .name("memberIdReader")
                .pageSize(1000)
                .entityManagerFactory(entityManagerFactory)
                .queryString("SELECT m.ID FROM MEMBER m")
                .build();
    }

    private ItemProcessor<Long, FrequentTags> viewedTagsProcessor() {
        return memberId -> {
            List<String> tagNames = getFrequentTagsOfViewedFeeds(memberId);
            return new FrequentTags(memberId, tagNames);
        };
    }

    private List<String> getFrequentTagsOfViewedFeeds(Long memberId) {
        return feedRepository.findAllFeedsByIdIn(viewedFeedsOfMember(memberId)).stream()
                .flatMap(feed -> flatTagNames(feed.getTagNames()).stream())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .limit(PageSize.FREQUENT_TAGS_SIZE)
                .toList();
    }

    private List<Long> viewedFeedsOfMember(Long memberId) {
        return recommendFeedsRepository.getViewedFeed(memberId);
    }

    private List<String> flatTagNames(String tagNamesJson){
        return jsonMapper.stringJsonToObject(tagNamesJson, new TypeReference<List<String>>() {});
    }
}
