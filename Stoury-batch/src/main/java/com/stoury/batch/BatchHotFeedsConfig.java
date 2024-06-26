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
import org.springframework.data.util.Pair;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.temporal.ChronoUnit;

@Configuration
@RequiredArgsConstructor
public class BatchHotFeedsConfig {
    private final LogJobExecutionListener logger;
    private final EntityManagerFactory entityManagerFactory;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ThreadPoolTaskExecutor taskExecutor;
    private final LikeRepository likeRepository;

    private final RankingRepository rankingRepository;

    @Bean
    @ConditionalOnExpression("'${spring.batch.job.names}'.contains('jobDailyFeeds')")
    public Job updateDailyFeedsJob() {
        return new JobBuilder("jobDailyFeeds", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(updateDailyFeedsStep())
                .next(initDailyLikeCountSnapshot())
                .listener(logger)
                .build();
    }

    @Bean
    @ConditionalOnExpression("'${spring.batch.job.names}'.contains('jobWeeklyFeeds')")
    public Job updateWeeklyFeedsJob() {
        return new JobBuilder("jobWeeklyFeeds", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(updateWeeklyFeedsStep())
                .next(initWeeklyLikeCountSnapshot())
                .listener(logger)
                .build();
    }

    @Bean
    @ConditionalOnExpression("'${spring.batch.job.names}'.contains('jobMonthlyFeeds')")
    public Job updateMonthlyFeedsJob() {
        return new JobBuilder("jobMonthlyFeeds", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(updateMonthlyFeedsStep())
                .next(initransactionManageronthlyLikeCountSnapshot())
                .listener(logger)
                .build();
    }

    @Bean
    @ConditionalOnExpression("'${spring.batch.job.names}'.contains('jobDailyFeeds')")
    public Step updateDailyFeedsStep() {
        return new StepBuilder("stepDailyFeed", jobRepository)
                .<Feed, Pair<SimpleFeedResponse, Long>>chunk(100, transactionManager)
                .reader(feedsReader())
                .processor(feedsProcessor(ChronoUnit.DAYS))
                .writer(feedsWriter(ChronoUnit.DAYS))
                .taskExecutor(taskExecutor)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    @ConditionalOnExpression("'${spring.batch.job.names}'.contains('jobWeeklyFeeds')")
    public Step updateWeeklyFeedsStep() {
        return new StepBuilder("stepWeeklyFeed", jobRepository)
                .<Feed, Pair<SimpleFeedResponse, Long>>chunk(100, transactionManager)
                .reader(feedsReader())
                .processor(feedsProcessor(ChronoUnit.WEEKS))
                .writer(feedsWriter(ChronoUnit.WEEKS))
                .taskExecutor(taskExecutor)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    @ConditionalOnExpression("'${spring.batch.job.names}'.contains('jobMonthlyFeeds')")
    public Step updateMonthlyFeedsStep() {
        return new StepBuilder("stepMonthlyFeed", jobRepository)
                .<Feed, Pair<SimpleFeedResponse, Long>>chunk(100, transactionManager)
                .reader(feedsReader())
                .processor(feedsProcessor(ChronoUnit.MONTHS))
                .writer(feedsWriter(ChronoUnit.MONTHS))
                .taskExecutor(taskExecutor)
                .allowStartIfComplete(true)
                .build();
    }


    @Bean
    @ConditionalOnExpression("'${spring.batch.job.names}'.contains('jobDailyFeeds')")
    public Step initDailyLikeCountSnapshot() {
        return new StepBuilder("stepInitDailyLikeCountSnapshot", jobRepository)
                .<Feed, Feed>chunk(100, transactionManager)
                .reader(feedsReader())
                .writer(likeInitializer(ChronoUnit.DAYS))
                .taskExecutor(taskExecutor)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    @ConditionalOnExpression("'${spring.batch.job.names}'.contains('jobWeeklyFeeds')")
    public Step initWeeklyLikeCountSnapshot() {
        return new StepBuilder("stepInitWeeklyLikeCountSnapshot", jobRepository)
                .<Feed, Feed>chunk(100, transactionManager)
                .reader(feedsReader())
                .writer(likeInitializer(ChronoUnit.WEEKS))
                .taskExecutor(taskExecutor)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    @ConditionalOnExpression("'${spring.batch.job.names}'.contains('jobMonthlyFeeds')")
    public Step initransactionManageronthlyLikeCountSnapshot() {
        return new StepBuilder("stepInitransactionManageronthlyLikeCountSnapshot", jobRepository)
                .<Feed, Feed>chunk(100, transactionManager)
                .reader(feedsReader())
                .writer(likeInitializer(ChronoUnit.MONTHS))
                .taskExecutor(taskExecutor)
                .allowStartIfComplete(true)
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

    public ItemProcessor<Feed, Pair<SimpleFeedResponse, Long>> feedsProcessor(ChronoUnit chronoUnit) {
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

    private ItemWriter<Feed> likeInitializer(ChronoUnit chronoUnit) {
        return list -> {
            for (Feed feed : list) {
                likeRepository.initCountSnapshotByFeed(feed.getId().toString(), chronoUnit);
            }
        };
    }

    public ItemWriter<Pair<SimpleFeedResponse, Long>> feedsWriter(ChronoUnit chronoUnit) {
        return list -> {
            for (Pair<SimpleFeedResponse, Long> pair : list) {
                SimpleFeedResponse rawSimpleFeed = pair.getFirst();
                long likeIncrease = pair.getSecond();

                if (likeIncrease > 0) {
                    rankingRepository.saveHotFeed(rawSimpleFeed, likeIncrease, chronoUnit);
                }
            }
        };
    }
}
