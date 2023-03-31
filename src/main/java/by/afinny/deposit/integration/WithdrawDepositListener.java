package by.afinny.deposit.integration;

import by.afinny.deposit.dto.kafka.ConsumerWithdrawEvent;
import by.afinny.deposit.service.AgreementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WithdrawDepositListener {

    private final AgreementService agreementService;

    @KafkaListener(
            topics = "${kafka.topics.withdraw-listener.path}",
            groupId = "deposit",
            containerFactory = "listenerFactory")
    public void onRequestUpdateAgreementStatusAndInsertOperationEvent(Message<ConsumerWithdrawEvent> message) {
        ConsumerWithdrawEvent event = message.getPayload();
        log.info("Processing event: " + event);
        agreementService.modifyAgreementAndCreateOperation(event);
    }
}
