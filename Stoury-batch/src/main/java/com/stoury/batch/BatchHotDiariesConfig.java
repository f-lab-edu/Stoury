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
import org.springframework.batch.core.launch.support.RunIdIncrementer;
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
@ConditionalOnExpression("'${spring.batch.job.names}'.contains('jobYearlyDiaries')")
public class BatchHotDiariesConfig {
    private static final int CHUNK_SIZE = 1000;
    private final LogJobExecutionListener logger;
    private final EntityManagerFactory entityManagerFactory;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ThreadPoolTaskExecutor taskExecutor;
    private final LikeRepository likeRepository;
    private final RankingRepository rankingRepository;

    @Bean
    public Job updateYearlyDiariesJob() {
        return new JobBuilder("jobYearlyDiaries", jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(updateYearlyDiariesStep())
                .next(initYearlyLikeCountSnapshot())
                .listener(logger)
                .build();
    }

    @Bean
    public Step updateYearlyDiariesStep() {
        return new StepBuilder("stepYearlyDiaries", jobRepository)
                .<Diary, Pair<SimpleDiaryResponse, Long>>chunk(CHUNK_SIZE, transactionManager)
                .reader(diaryReader())
                .processor(diaryProcessor())
                .writer(diaryWriter())
                .taskExecutor(taskExecutor)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Step initYearlyLikeCountSnapshot() {
        return new StepBuilder("stepInitLikeCountSnapshot", jobRepository)
                .<Diary, Diary>chunk(CHUNK_SIZE, transactionManager)
                .reader(diaryReader())
                .writer(likeInitializer(likeRepository))
                .taskExecutor(taskExecutor)
                .allowStartIfComplete(true)
                .build();
    }

    public JpaPagingItemReader<Diary> diaryReader() {
        return new JpaPagingItemReaderBuilder<Diary>()
                .name("diaryReader")
                .pageSize(CHUNK_SIZE)
                .entityManagerFactory(entityManagerFactory)
                .queryString("select d from Diary d")
                .build();
    }

    public ItemProcessor<Diary, Pair<SimpleDiaryResponse, Long>> diaryProcessor() {
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
