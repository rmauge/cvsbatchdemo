package com.rmauge.cvsdemo.order.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rmauge.cvsdemo.order.model.Bill;
import com.rmauge.cvsdemo.order.model.Order;
import com.rmauge.cvsdemo.order.model.OrderProcessor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.json.JacksonJsonObjectReader;
import org.springframework.batch.item.json.JsonItemReader;
import org.springframework.batch.item.json.builder.JsonItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.task.configuration.EnableTask;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import javax.sql.DataSource;

@Configuration
@EnableTask
@EnableBatchProcessing
public class OrderConfiguration {

    @Autowired
    JobBuilderFactory jobBuilderFactory;

    @Autowired
    StepBuilderFactory stepBuilderFactory;

    @Value("${usage.file.name:classpath:orders.json}")
    private Resource usageResource;

    @Bean
    public JsonItemReader<Order> jsonItemReader() {
        ObjectMapper objectMapper = new ObjectMapper();
        JacksonJsonObjectReader<Order> jsonObjectReader =
                new JacksonJsonObjectReader<>(Order.class);
        jsonObjectReader.setMapper(objectMapper);

        return new JsonItemReaderBuilder<Order>()
                .jsonObjectReader(jsonObjectReader)
                .resource(usageResource)
                .name("OrderJsonItemReader")
                .build();
    }

    @Bean
    public ItemWriter<Bill> jdbcBillWriter(DataSource dataSource) {
        JdbcBatchItemWriter<Bill> writer = new JdbcBatchItemWriterBuilder<Bill>()
                .beanMapped()
                .dataSource(dataSource)
                .sql("INSERT INTO ORDER_STATEMENTS (id, total) VALUES (:id, :total)")
                .build();

        return writer;
    }

    @Bean
    public ItemProcessor<Order, Bill> orderProcessor() {
        return new OrderProcessor();
    }

    @Bean
    public Job job1(ItemReader<Order> orderItemReader,
                    ItemProcessor<Order, Bill> itemProcessor,
                    ItemWriter<Bill> itemWriter) {
        Step step1 = stepBuilderFactory.get("OrderProcessing")
                .<Order, Bill> chunk(1)
                .reader(orderItemReader)
                .processor(itemProcessor)
                .writer(itemWriter)
                .build();

        return jobBuilderFactory.get("BillJob")
                .incrementer(new RunIdIncrementer())
                .start(step1)
                .build();
    }
}
