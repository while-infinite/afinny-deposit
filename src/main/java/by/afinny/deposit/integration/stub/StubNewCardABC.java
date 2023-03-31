package by.afinny.deposit.integration.stub;

import by.afinny.deposit.dto.kafka.ConsumerNewCardEvent;
import by.afinny.deposit.dto.kafka.ProducerNewCardEvent;
import by.afinny.deposit.entity.Account;
import by.afinny.deposit.entity.constant.CardStatus;
import by.afinny.deposit.entity.constant.DigitalWallet;
import by.afinny.deposit.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class StubNewCardABC {

    private final KafkaTemplate<String, ?> kafkaTemplate;
    private final AccountRepository accountRepository;

    @Value("${kafka.topics.new-card-listener.path}")
    private String topic;


    @KafkaListener(
            topics = "${kafka.topics.new-card-producer.path}",
            groupId = "deposit-service",
            containerFactory = "stubKafkaListenerNewCard"
    )
    public void receiveProducerAndSendConsumerCardEvent(Message<ProducerNewCardEvent> message) {
        log.info("receiveProducerAndSendConsumerCardEvent() method invoked");
        ProducerNewCardEvent producerNewCardEvent = message.getPayload();
        ConsumerNewCardEvent consumerNewCardEvent = setUpEvent(producerNewCardEvent);
        sendEvent(consumerNewCardEvent);
    }

    private void sendEvent(ConsumerNewCardEvent event) {
        log.debug("sendEvent() method invoked");
        log.debug("send event: " + event);
        kafkaTemplate.send(
                MessageBuilder
                        .withPayload(event)
                        .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                        .setHeader(KafkaHeaders.TOPIC, topic)
                        .build());
    }

    private ConsumerNewCardEvent setUpEvent(ProducerNewCardEvent event) {
        log.debug("setUpEvent() method invoked");
        return ConsumerNewCardEvent.builder()
                .cardNumber(generateRandomInts(16))
                .accountNumber(randomAccountNumber(event.getClientId()))
                .cardProductId(event.getRequestNewCardDto().getProductId())
                .expirationDate(LocalDate.now().plusYears(5))
                .holderName("HN test " + LocalDateTime.now())
                .status(CardStatus.ACTIVE)
                .transactionLimit(BigDecimal.valueOf(Integer.parseInt(generateRandomInts(5))))
                .digitalWallet(randomWallet())
                .balance(new BigDecimal(0))
                .build();
    }

    private String randomAccountNumber(UUID clientId) {
        List<Account> accounts = accountRepository.findByClientIdAndIsActiveTrue(clientId);
        int choice = getChoice(accounts.size());
        return accounts.get(choice).getAccountNumber();
    }


    private DigitalWallet randomWallet() {
        int choice = getChoice(DigitalWallet.values().length);
        return DigitalWallet.values()[choice];
    }

    @SneakyThrows
    private int getChoice(int length) {
        Random random = SecureRandom.getInstanceStrong();
        return random.nextInt(length);
    }

    @SneakyThrows
    private String generateRandomInts(int length) {
        Random random = SecureRandom.getInstanceStrong();
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < length; i++) {
            s.append(random.nextInt(10));
        }
        return s.toString();
    }
}
