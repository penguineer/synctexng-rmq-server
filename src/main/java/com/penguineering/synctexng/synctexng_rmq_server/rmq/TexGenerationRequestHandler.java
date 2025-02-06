package com.penguineering.synctexng.synctexng_rmq_server.rmq;

import com.penguineering.synctexng.synctexng_rmq_server.archive.RequestArchiveExtractor;
import com.penguineering.synctexng.synctexng_rmq_server.archive.ResultArchiveCompressor;
import com.penguineering.synctexng.synctexng_rmq_server.latex.LatexExecutor;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.api.ChannelAwareMessageListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

@Component
public class TexGenerationRequestHandler implements ChannelAwareMessageListener {
    private static final Logger logger = LoggerFactory.getLogger(TexGenerationRequestHandler.class);

    private final RabbitTemplate rabbitTemplate;

    public TexGenerationRequestHandler(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void onMessage(Message message, Channel channel) {
        try {
            logger.info("Received message: {}", message);

            // Extract the correlation ID
            final Optional<String> correlationId = getCorrelationId(message);
            correlationId.ifPresentOrElse(
                    id -> logger.info("Received a message with Correlation ID: {}", id),
                    () -> logger.warn("Received a message without Correlation ID")
            );

            // Extract the "reply_to" property
            String replyTo = Optional
                    .ofNullable(message.getMessageProperties())
                    .map(MessageProperties::getReplyTo)
                    .orElseThrow(() -> new IllegalArgumentException("Reply_to property is missing"));
            logger.info("Reply-to: {}", replyTo);

            // receive message payload as byte[]
            byte[] data = message.getBody();

            // Execute the LaTeX build
            byte[] response = executeLatexBuild(data);

            // Send the result
            MessageProperties messageProperties = new MessageProperties();
            correlationId.ifPresent(messageProperties::setCorrelationId);
            messageProperties.setContentType("application/octet-stream");
            Message responseMessage = new Message(response, messageProperties);

            rabbitTemplate.send(replyTo, responseMessage);
            ackMessage(message, channel);

        } catch (IllegalArgumentException e) {
            logger.info("Illegal argument exception on message handling: ", e);
            nackMessage(message, channel, false);
        } catch (IOException e) {
            logger.error("IO Exception on message handling: ", e);
            nackMessage(message, channel, true);
        }
    }

    private Optional<String> getCorrelationId(Message message) {
        return Optional
                .ofNullable(message.getMessageProperties())
                .map(MessageProperties::getCorrelationId);
    }

    private void ackMessage(Message message, Channel channel) {
        if (Objects.nonNull(channel))
            try {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                channel.txCommit();
            } catch (IOException e) {
                logger.error("Failed to ack message", e);
            } catch (Exception e) {
                logger.error("Failed to handle response", e);
            }
    }

    private void nackMessage(Message message, Channel channel, boolean requeue) {
        if (Objects.nonNull(channel))
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, requeue);
                channel.txCommit();
            } catch (IOException e) {
                logger.error("Failed to ack message", e);
            } catch (Exception e) {
                logger.error("Failed to handle response", e);
            }
    }

    private byte[] executeLatexBuild(byte[] input) throws IOException {
        // Create temporary directory
        Path tempDir;
        tempDir = Files.createTempDirectory("synctexng");
        logger.info("Temp dir: {}", tempDir);

        // Process the zip archive
        RequestArchiveExtractor extractor = new RequestArchiveExtractor(tempDir);
        extractor.unpack(input);

        Path texRoot = extractor.getRootTexFile();

        if (Objects.isNull(texRoot))
            throw new IllegalArgumentException("No .tex file found in zip archive");
        logger.info("Tex root: {}", extractor.getRootTexFile());
        Path nameRoot = texRoot.resolveSibling(texRoot.getFileName().toString().replace(".tex", ""));

        // Create result archive
        ResultArchiveCompressor compressor = new ResultArchiveCompressor(tempDir);

        // Compile the LaTeX document
        LatexExecutor latexExecutor = new LatexExecutor(tempDir, nameRoot, compressor::addFilePath);
        latexExecutor.compileDocument();

        return compressor.createZipInMemory();

        // TODO delete the temp directory
    }
}
