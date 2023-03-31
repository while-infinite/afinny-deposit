package by.afinny.deposit.integration;


import by.afinny.deposit.dto.kafka.CardEvent;
import by.afinny.deposit.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CardStatusTopicListener {

    private final CardService cardService;

    @KafkaListener(
            topics = "${kafka.topics.bank-system-card-status-listener.path}",
            groupId = "deposit-service",
            containerFactory = "listenerFactoryForCardStatus")
    public void onRequestUpdateCardStatusEvent(Message<CardEvent> message) {
        CardEvent event = message.getPayload();
        log.info("Processing event: card number = " + event.getCardId() + ", card status = " + event.getCardStatus());
        cardService.modifyCardStatus(event.getCardId(), event.getCardStatus());
    }
}
