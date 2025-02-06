package com.penguineering.synctexng.synctexng_rmq_server.rmq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {
    private static final Logger logger = LoggerFactory.getLogger(RabbitMQConfig.class);

    @Value("${synctexng.server.queue-tex-requests}")
    private String queueTexRequests;

    @Bean
    public Queue texQueue() {
        return QueueBuilder.durable(queueTexRequests)
                .build();
    }

    @Bean
    public SimpleMessageListenerContainer texContainer(ConnectionFactory connectionFactory,
                                                       TexGenerationRequestHandler handler) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueTexRequests);
        container.setMessageListener(handler);
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        container.setChannelTransacted(true);
        return container;
    }
}
