package com.sanos.auditservice.messaging;

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
    Queue auditReportCreatedQueue() {
        return new Queue(SanosMessaging.QUEUE_AUDIT_REPORT_CREATED, true);
    }

    @Bean
    Binding auditReportCreatedBinding(Queue auditReportCreatedQueue, TopicExchange sanosEventsExchange) {
        return BindingBuilder
                .bind(auditReportCreatedQueue)
                .to(sanosEventsExchange)
                .with(SanosMessaging.ROUTING_REPORT_CREATED);
    }

    @Bean
    MessageConverter jacksonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
