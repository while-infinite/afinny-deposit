package by.afinny.deposit.integration;

import by.afinny.deposit.dto.kafka.ProducerWithdrawEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
public class WithdrawSource {

    private final KafkaTemplate<String, ?> kafkaTemplate;

    @Value("${kafka.topics.withdraw-producer.path}")
    private String topic;

    @EventListener
    public void sendMessageAboutWithdrawDeposit(ProducerWithdrawEvent event) {
        log.info("Event " + event + " has been received, sending message...");
        kafkaTemplate.send(
                MessageBuilder
                        .withPayload(event)
                        .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                        .setHeader(KafkaHeaders.TOPIC, topic)
                        .build()
        );
    }
}
