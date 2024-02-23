package com.stoury.batch;

import com.stoury.domain.Diary;
import com.stoury.domain.Feed;
import com.stoury.dto.diary.SimpleDiaryResponse;
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
import org.springframework.batch.item.ItemStreamWriter;
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
@ConditionalOnExpression("'${spring.batch.job.names}'.contains('updateHotDiariesJob')")
public class BatchHotDiariesConfig {
    private final EntityManagerFactory entityManagerFactory;
    private final RankingRepository rankingRepository;

    @Bean
    public Job updateYearlyDiariesJob(JobRepository jobRepository, PlatformTransactionManager tm,
                                    ThreadPoolTaskExecutor taskExecutor, LikeRepository likeRepository) {
        return new JobBuilder("jobYearlyDiaries", jobRepository)
                .start(updateYearlyDiariesStep(jobRepository, tm, taskExecutor, likeRepository))
                .next(initYearlyLikeCountSnapshot(jobRepository, tm, taskExecutor, likeRepository))
                .build();
    }

    @Bean
    public Step updateYearlyDiariesStep(JobRepository jobRepository, PlatformTransactionManager tm, ThreadPoolTaskExecutor taskExecutor, LikeRepository likeRepository) {
        return new StepBuilder("stepYearlyDiaries", jobRepository)
                .<Diary, Pair<SimpleDiaryResponse, Long>>chunk(100, tm)
                .reader(diaryReader())
                .processor(diaryProcessor(likeRepository))
                .writer(diaryWriter())
                .taskExecutor(taskExecutor)
                .build();
    }

    @Bean
    public Step initYearlyLikeCountSnapshot(JobRepository jobRepository, PlatformTransactionManager tm,
                                           ThreadPoolTaskExecutor taskExecutor, LikeRepository likeRepository) {
        return new StepBuilder("stepInitLikeCountSnapshot", jobRepository)
                .<Diary, Diary>chunk(100, tm)
                .reader(diaryReader())
                .writer(likeInitializer(likeRepository))
                .taskExecutor(taskExecutor)
                .build();
    }

    public JpaPagingItemReader<Diary> diaryReader() {
        return new JpaPagingItemReaderBuilder<Diary>()
                .name("diaryReader")
                .pageSize(100)
                .entityManagerFactory(entityManagerFactory)
                .queryString("select d from Diary d")
                .build();
    }

    public ItemProcessor<Diary, Pair<SimpleDiaryResponse, Long>> diaryProcessor(LikeRepository likeRepository) {
        return diary -> {
            long likeIncrease = diary.getFeeds().stream()
                    .mapToLong(Feed::getId)
                    .map(feedId -> {
                        long currentLikes = likeRepository.getCountByFeedId(String.valueOf(feedId));
                        long prevLikes = likeRepository.getCountSnapshotByFeed(String.valueOf(feedId), ChronoUnit.YEARS);
                        return currentLikes - prevLikes;
                    })
                    .sum();
            SimpleDiaryResponse simpleDiaryResponse = SimpleDiaryResponse.from(diary);
            return Pair.of(simpleDiaryResponse, likeIncrease);
        };
    }

    public ItemWriter<Pair<SimpleDiaryResponse, Long>> diaryWriter() {
        return list -> {
            for (Pair<SimpleDiaryResponse, Long> pair : list) {
                SimpleDiaryResponse rawSimpleDiary = pair.getFirst();
                Long likeIncrease = pair.getSecond();

                if (likeIncrease > 0) {
                    rankingRepository.saveHotDiaries(rawSimpleDiary, likeIncrease);
                }
            }
        };
    }

    public ItemStreamWriter<Diary> likeInitializer(LikeRepository likeRepository) {
        return list -> {
            for (Diary diary : list) {
                for (Feed feed : diary.getFeeds()) {
                    likeRepository.initCountSnapshotByFeed(feed.getId().toString(), ChronoUnit.YEARS);
                }
            }
        };
    }
}
