package com.penguineering.synctexng.synctexng_rmq_server.rmq;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

@Component
public class TexGenerationRequestHandler implements ChannelAwareMessageListener {
    private static final Logger logger = LoggerFactory.getLogger(TexGenerationRequestHandler.class);

    @Override
    public void onMessage(Message message, Channel channel) {
        logger.info("Received message: {}", message);
        try {
            if (Objects.nonNull(channel)) {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                channel.txCommit();
            }
        } catch (IOException e) {
            logger.error("Failed to ack message", e);
        } catch (Exception e) {
            logger.error("Failed to handle response", e);
        }
    }
}
