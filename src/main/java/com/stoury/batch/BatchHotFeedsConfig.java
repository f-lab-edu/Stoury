package com.stoury.batch;

import com.stoury.domain.Feed;
import com.stoury.dto.feed.SimpleFeedResponse;
import com.stoury.repository.LikeRepository;
import com.stoury.repository.RankingRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.temporal.ChronoUnit;

@Configuration
@RequiredArgsConstructor
@ConditionalOnExpression("'${spring.batch.job.names}'.contains('updateHotFeedsJob')")
public class BatchHotFeedsConfig {
    private final EntityManagerFactory entityManagerFactory;

    private final RankingRepository rankingRepository;

    @Bean
    public Job updateDailyFeedsJob(JobRepository jobRepository, PlatformTransactionManager tm,
                                   ThreadPoolTaskExecutor taskExecutor, LikeRepository likeRepository) {
        return new JobBuilder("jobDailyFeed", jobRepository)
                .start(updateDailyFeedsStep(jobRepository, tm, taskExecutor, likeRepository))
                .build();
    }

    @Bean
    public Job updateWeeklyFeedsJob(JobRepository jobRepository, PlatformTransactionManager tm,
                                    ThreadPoolTaskExecutor taskExecutor, LikeRepository likeRepository) {
        return new JobBuilder("jobWeeklyFeed", jobRepository)
                .start(updateWeeklyFeedsStep(jobRepository, tm, taskExecutor, likeRepository))
                .build();
    }

    @Bean
    public Job updateMonthlyFeedsJob(JobRepository jobRepository, PlatformTransactionManager tm,
                                     ThreadPoolTaskExecutor taskExecutor, LikeRepository likeRepository) {
        return new JobBuilder("jobMonthlyFeed", jobRepository)
                .start(updateMonthlyFeedsStep(jobRepository, tm, taskExecutor, likeRepository))
                .build();
    }

    @Bean
    public Step updateDailyFeedsStep(JobRepository jobRepository, PlatformTransactionManager tm,
                                     ThreadPoolTaskExecutor taskExecutor, LikeRepository likeRepository) {
        return new StepBuilder("stepDailyFeed", jobRepository)
                .<Feed, Pair<SimpleFeedResponse, Long>>chunk(100, tm)
                .reader(feedsReader())
                .processor(feedsProcessor(likeRepository, ChronoUnit.DAYS))
                .writer(feedsWriter(ChronoUnit.DAYS))
                .taskExecutor(taskExecutor)
                .build();
    }

    @Bean
    public Step updateWeeklyFeedsStep(JobRepository jobRepository, PlatformTransactionManager tm,
                                      ThreadPoolTaskExecutor taskExecutor, LikeRepository likeRepository) {
        return new StepBuilder("stepWeeklyFeed", jobRepository)
                .<Feed, Pair<SimpleFeedResponse, Long>>chunk(100, tm)
                .reader(feedsReader())
                .processor(feedsProcessor(likeRepository, ChronoUnit.WEEKS))
                .writer(feedsWriter(ChronoUnit.WEEKS))
                .taskExecutor(taskExecutor)
                .build();
    }

    @Bean
    public Step updateMonthlyFeedsStep(JobRepository jobRepository, PlatformTransactionManager tm,
                                       ThreadPoolTaskExecutor taskExecutor, LikeRepository likeRepository) {
        return new StepBuilder("stepMonthlyFeed", jobRepository)
                .<Feed, Pair<SimpleFeedResponse, Long>>chunk(100, tm)
                .reader(feedsReader())
                .processor(feedsProcessor(likeRepository, ChronoUnit.MONTHS))
                .writer(feedsWriter(ChronoUnit.MONTHS))
                .taskExecutor(taskExecutor)
                .build();
    }


    public JpaPagingItemReader<Feed> feedsReader() {
        return new JpaPagingItemReaderBuilder<Feed>()
                .name("feedReader")
                .pageSize(100)
                .entityManagerFactory(entityManagerFactory)
                .queryString("select f from Feed f")
                .build();
    }

    public ItemProcessor<Feed, Pair<SimpleFeedResponse, Long>> feedsProcessor(LikeRepository likeRepository, ChronoUnit chronoUnit) {
        return feed -> {
            Long feedId = feed.getId();

            if (!likeRepository.existsByFeedId(feedId.toString())) {
                return null;
            }
            long currentLikes = likeRepository.getCountByFeedId(feedId.toString());
            long prevLikes = likeRepository.getCountSnapshotByFeed(feedId.toString(), chronoUnit);

            SimpleFeedResponse simpleFeed = SimpleFeedResponse.from(feed);

            return Pair.of(simpleFeed, currentLikes - prevLikes);
        };
    }

    public ItemWriter<Pair<SimpleFeedResponse, Long>> feedsWriter(ChronoUnit chronoUnit) {
        return list -> {
            for (Pair<SimpleFeedResponse, Long> pair : list) {
                SimpleFeedResponse rawSimpleFeed = pair.getFirst();
                Long likeIncrease = pair.getSecond();

                if (likeIncrease > 0) {
                    rankingRepository.saveHotFeed(rawSimpleFeed, likeIncrease, chronoUnit);
                }
            }
        };
    }
}
