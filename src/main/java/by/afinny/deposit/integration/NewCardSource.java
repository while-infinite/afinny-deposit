package by.afinny.deposit.integration;

import by.afinny.deposit.dto.kafka.ProducerNewCardEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty("kafka.topics.new-card-producer.enabled")
public class NewCardSource {

    private final KafkaTemplate<String, ?> kafkaTemplate;

    @Value("${kafka.topics.new-card-producer.path}")
    private String topic;

    @EventListener
    public void sendMessageAboutNewCard(ProducerNewCardEvent event) {
        log.info("Event {} has been received, sending message...", event);
        kafkaTemplate.send(
                MessageBuilder
                        .withPayload(event)
                        .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                        .setHeader(KafkaHeaders.TOPIC, topic)
                        .build()
        );
    }
}
