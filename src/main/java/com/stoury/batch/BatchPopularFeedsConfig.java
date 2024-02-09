package com.stoury.batch;

import com.stoury.domain.Feed;
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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.temporal.ChronoUnit;

@Configuration
@RequiredArgsConstructor
public class BatchPopularFeedsConfig {
    private final EntityManagerFactory entityManagerFactory;

    private final RankingRepository rankingRepository;

    @Bean
    public Job jobDailyFeed(JobRepository jobRepository, PlatformTransactionManager tm,
                            ThreadPoolTaskExecutor taskExecutor, LikeRepository likeRepository) {
        return new JobBuilder("jobDailyFeed", jobRepository)
                .start(stepDailyFeed(jobRepository, tm, taskExecutor, likeRepository))
                .build();
    }

    @Bean
    public Job jobWeeklyFeed(JobRepository jobRepository, PlatformTransactionManager tm,
                             ThreadPoolTaskExecutor taskExecutor, LikeRepository likeRepository) {
        return new JobBuilder("jobWeeklyFeed", jobRepository)
                .start(stepWeeklyFeed(jobRepository, tm, taskExecutor, likeRepository))
                .build();
    }

    @Bean
    public Job jobMonthlyFeed(JobRepository jobRepository, PlatformTransactionManager tm,
                              ThreadPoolTaskExecutor taskExecutor, LikeRepository likeRepository) {
        return new JobBuilder("jobMonthlyFeed", jobRepository)
                .start(stepMonthlyFeed(jobRepository, tm, taskExecutor, likeRepository))
                .build();
    }

    @Bean
    public Step stepDailyFeed(JobRepository jobRepository, PlatformTransactionManager tm,
                              ThreadPoolTaskExecutor taskExecutor, LikeRepository likeRepository) {
        return new StepBuilder("stepDailyFeed", jobRepository)
                .<Feed, Pair<String, Long>>chunk(100, tm)
                .reader(feedReader())
                .processor(feedProcessor(likeRepository, ChronoUnit.DAYS))
                .writer(feedWriter(ChronoUnit.DAYS))
                .taskExecutor(taskExecutor)
                .build();
    }

    @Bean
    public Step stepWeeklyFeed(JobRepository jobRepository, PlatformTransactionManager tm,
                               ThreadPoolTaskExecutor taskExecutor, LikeRepository likeRepository) {
        return new StepBuilder("stepWeeklyFeed", jobRepository)
                .<Feed, Pair<String, Long>>chunk(100, tm)
                .reader(feedReader())
                .processor(feedProcessor(likeRepository, ChronoUnit.WEEKS))
                .writer(feedWriter(ChronoUnit.WEEKS))
                .taskExecutor(taskExecutor)
                .build();
    }

    @Bean
    public Step stepMonthlyFeed(JobRepository jobRepository, PlatformTransactionManager tm,
                                ThreadPoolTaskExecutor taskExecutor, LikeRepository likeRepository) {
        return new StepBuilder("stepMonthlyFeed", jobRepository)
                .<Feed, Pair<String, Long>>chunk(100, tm)
                .reader(feedReader())
                .processor(feedProcessor(likeRepository, ChronoUnit.MONTHS))
                .writer(feedWriter(ChronoUnit.MONTHS))
                .taskExecutor(taskExecutor)
                .build();
    }


    public JpaPagingItemReader<Feed> feedReader() {
        return new JpaPagingItemReaderBuilder<Feed>()
                .name("feedReader")
                .pageSize(100)
                .entityManagerFactory(entityManagerFactory)
                .queryString("select f from Feed f")
                .build();
    }

    public ItemProcessor<Feed, Pair<String, Long>> feedProcessor(LikeRepository likeRepository, ChronoUnit chronoUnit) {
        return feed -> {
            String feedId = feed.getId().toString();

            if (!likeRepository.existsByFeedId(feedId)) {
                return Pair.of(feedId, 0L);
            }
            long currentLikes = likeRepository.getCountByFeedId(feedId);
            long prevLikes = likeRepository.getCountSnapshotByFeed(feedId, chronoUnit);

            return Pair.of(feedId, currentLikes - prevLikes);
        };
    }

    public ItemWriter<Pair<String, Long>> feedWriter(ChronoUnit chronoUnit) {
        return list -> {
            for (Pair<String, Long> pair : list) {
                String feedId = pair.getFirst();
                Long likeIncrease = pair.getSecond();

                if (likeIncrease > 0) {
                    rankingRepository.saveHotFeed(feedId, likeIncrease, chronoUnit);
                }
            }
        };
    }
}
