package by.afinny.deposit.integration;

import by.afinny.deposit.dto.kafka.ConsumerNewCardEvent;
import by.afinny.deposit.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NewCardListener {

    private final OrderService orderService;

    @KafkaListener(
            topics = "${kafka.topics.new-card-listener.path}",
            groupId = "deposit-service",
            containerFactory = "kafkaListenerNewCard")
    public void onRequestInsertNewCard(Message<ConsumerNewCardEvent> message) {
        ConsumerNewCardEvent event = message.getPayload();
        log.info("Processing event: {}", event);
        orderService.createNewCard(event);
    }
}
