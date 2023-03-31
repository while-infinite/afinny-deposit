package by.afinny.deposit.integration;

import by.afinny.deposit.entity.Agreement;
import by.afinny.deposit.service.DepositService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DepositTopicListener {

    private final DepositService depositService;

    @KafkaListener(
            topics = "${kafka.topics.deposit-service-listener.path}",
            groupId = "deposit-service",
            containerFactory = "listenerFactory")
    public void receiveDeposit(Message<Agreement> message) {
        log.info("receiveDeposit() method invoke");
        Agreement agreement = message.getPayload();
        depositService.saveAgreement(agreement);
    }
}
