package com.sanos.matchingservice.messaging;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMqConfig {

    @Bean
    TopicExchange sanosEventsExchange() {
        return new TopicExchange(SanosMessaging.EXCHANGE, true, false);
    }

    @Bean
    Queue matchingReportCreatedQueue() {
        return new Queue(SanosMessaging.QUEUE_MATCHING_REPORT_CREATED, true);
    }

    @Bean
    Binding matchingReportCreatedBinding(Queue matchingReportCreatedQueue, TopicExchange sanosEventsExchange) {
        return BindingBuilder
                .bind(matchingReportCreatedQueue)
                .to(sanosEventsExchange)
                .with(SanosMessaging.ROUTING_REPORT_CREATED);
    }

    @Bean
    MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
