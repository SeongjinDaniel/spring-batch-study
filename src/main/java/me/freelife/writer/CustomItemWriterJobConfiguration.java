package me.freelife.writer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.freelife.reader.jdbc.Pay;
import me.freelife.reader.jdbc.Pay2;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.persistence.EntityManagerFactory;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class CustomItemWriterJobConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    private static final int chunkSize = 10;

    @Bean
    public Job customItemWriterJob() {
        return jobBuilderFactory.get("customItemWriterJob")
            .start(customItemWriterStep())
            .build();
    }

    @Bean
    public Step customItemWriterStep() {
        return stepBuilderFactory.get("customItemWriterStep")
            .<Pay, Pay2>chunk(chunkSize)
            .reader(customItemWriterReader())
            .processor(customItemWriterProcessor())
            .writer(customItemWriter())
            .build();
    }

    @Bean
    public JpaPagingItemReader<Pay> customItemWriterReader() {
        return new JpaPagingItemReaderBuilder<Pay>()
            .name("customItemWriterReader")
            .entityManagerFactory(entityManagerFactory)
            .pageSize(chunkSize)
            .queryString("SELECT p FROM Pay p")
            .build();
    }

    @Bean
    public ItemProcessor<Pay, Pay2> customItemWriterProcessor() {
        return pay -> new Pay2(pay.getAmount(), pay.getTxName(), pay.getTxDateTime());
    }

    /**
     * 자바7 이하일 경우
     */
//    @Bean
//    public ItemWriter<Pay2> customItemWriter() {
//        return new ItemWriter<Pay2>() {
//            @Override
//            public void write(List<? extends Pay2> items) throws Exception {
//                for (Pay2 item : items) {
//                    System.out.println(item);
//                }
//            }
//        };
//    }

    /**
     * 자바 8이상이면 이렇게 람다식을 사용하면 더 깔끔함
     */
    @Bean
    public ItemWriter<Pay2> customItemWriter() {
        return items -> {
            for (Pay2 item : items) {
                System.out.println(item);
            }
        };
    }
}
