package com.stoury.batch;

import com.stoury.domain.Feed;
import com.stoury.repository.FeedRepository;
import com.stoury.repository.LikeRepository;
import com.stoury.repository.RankingRepository;
import com.stoury.utils.CacheKeys;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.temporal.ChronoUnit;
import java.util.List;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class BatchConfig {
    Pageable pageable = PageRequest.of(0, 10);
    private final FeedRepository feedRepository;
    private final RankingRepository rankingRepository;
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Step stepUpdatePopularDomesticCities(JobRepository jobRepository, PlatformTransactionManager tm) {
        return new StepBuilder("stepUpdatePopularDomesticCities", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    List<String> rankedDomesticCities = feedRepository.findTop10CitiesInKorea(pageable);
                    rankingRepository.update(CacheKeys.POPULAR_DOMESTIC_SPOTS, rankedDomesticCities);
                    return RepeatStatus.FINISHED;
                }, tm)
                .build();
    }

    @Bean
    public Step stepUpdatePopularAbroadCities(JobRepository jobRepository, PlatformTransactionManager tm) {
        return new StepBuilder("stepUpdatePopularAbroadCities", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    List<String> rankedCountries = feedRepository.findTop10CountriesNotKorea(pageable);
                    rankingRepository.update(CacheKeys.POPULAR_ABROAD_SPOTS, rankedCountries);
                    return RepeatStatus.FINISHED;
                }, tm)
                .build();
    }

    @Bean
    public Job jobUpdatePopularSpots(JobRepository jobRepository, PlatformTransactionManager tm,
                                     ThreadPoolTaskExecutor taskExecutor) {
        Flow flowUpdatePopularAbroadCities = new FlowBuilder<Flow>("flowUpdatePopularAbroadCities")
                .start(stepUpdatePopularAbroadCities(jobRepository, tm))
                .build();
        Flow flowUpdatePopularDomesticCities = new FlowBuilder<Flow>("flowUpdatePopularDomesticCities")
                .start(stepUpdatePopularDomesticCities(jobRepository, tm))
                .build();

        return new JobBuilder("updatePopularSpots", jobRepository)
                .start(flowUpdatePopularAbroadCities)
                .split(taskExecutor)
                .add(flowUpdatePopularDomesticCities)
                .end()
                .build();
    }

    @Bean
    public JpaPagingItemReader<Feed> feedReader() {
        return new JpaPagingItemReaderBuilder<Feed>()
                .name("feedReader")
                .pageSize(100)
                .entityManagerFactory(entityManagerFactory)
                .queryString("select f from Feed f")
                .build();
    }

    @Bean
    public ItemProcessor<Feed, Pair<String, Long>> dailyFeedProcessor(LikeRepository likeRepository) {
        return feedProcessor(likeRepository, ChronoUnit.DAYS);
    }

    @Bean
    public ItemProcessor<Feed, Pair<String, Long>> weeklyFeedProcessor(LikeRepository likeRepository) {
        return feedProcessor(likeRepository, ChronoUnit.WEEKS);
    }

    @Bean
    public ItemProcessor<Feed, Pair<String, Long>> monthlyFeedProcessor(LikeRepository likeRepository) {
        return feedProcessor(likeRepository, ChronoUnit.MONTHS);
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

    @Bean
    public ItemWriter<Pair<String, Long>> dailyFeedWriter() {
        return feedWriter(ChronoUnit.DAYS);
    }

    @Bean
    public ItemWriter<Pair<String, Long>> weeklyFeedWriter() {
        return feedWriter(ChronoUnit.WEEKS);
    }

    @Bean
    public ItemWriter<Pair<String, Long>> monthlyFeedWriter() {
        return feedWriter(ChronoUnit.MONTHS);
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

    @Bean
    public Step stepDailyFeed(JobRepository jobRepository, PlatformTransactionManager tm,
                              ThreadPoolTaskExecutor taskExecutor, LikeRepository likeRepository) {
        return new StepBuilder("stepDailyFeed", jobRepository)
                .<Feed, Pair<String, Long>>chunk(100, tm)
                .reader(feedReader())
                .processor(dailyFeedProcessor(likeRepository))
                .writer(dailyFeedWriter())
                .taskExecutor(taskExecutor)
                .build();
    }

    @Bean
    public Step stepWeeklyFeed(JobRepository jobRepository, PlatformTransactionManager tm,
                              ThreadPoolTaskExecutor taskExecutor, LikeRepository likeRepository) {
        return new StepBuilder("stepWeeklyFeed", jobRepository)
                .<Feed, Pair<String, Long>>chunk(100, tm)
                .reader(feedReader())
                .processor(weeklyFeedProcessor(likeRepository))
                .writer(weeklyFeedWriter())
                .taskExecutor(taskExecutor)
                .build();
    }

    @Bean
    public Step stepMonthlyFeed(JobRepository jobRepository, PlatformTransactionManager tm,
                              ThreadPoolTaskExecutor taskExecutor, LikeRepository likeRepository) {
        return new StepBuilder("stepMonthlyFeed", jobRepository)
                .<Feed, Pair<String, Long>>chunk(100, tm)
                .reader(feedReader())
                .processor(monthlyFeedProcessor(likeRepository))
                .writer(monthlyFeedWriter())
                .taskExecutor(taskExecutor)
                .build();
    }

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
}
