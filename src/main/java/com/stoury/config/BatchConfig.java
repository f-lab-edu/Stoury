package com.stoury.config;

import com.stoury.repository.FeedRepository;
import com.stoury.repository.RankingRepository;
import com.stoury.utils.CacheKeys;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class BatchConfig {
    Pageable pageable = PageRequest.of(0, 10);
    private final FeedRepository feedRepository;
    private final RankingRepository rankingRepository;

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
}
