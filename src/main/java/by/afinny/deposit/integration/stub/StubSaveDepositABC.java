package by.afinny.deposit.integration.stub;

import by.afinny.deposit.dto.RequestNewDepositDto;
import by.afinny.deposit.entity.Account;
import by.afinny.deposit.entity.Agreement;
import by.afinny.deposit.entity.Card;
import by.afinny.deposit.entity.Product;
import by.afinny.deposit.repository.CardRepository;
import by.afinny.deposit.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.utility.RandomString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * заглушка для формирования соглашения (Agreement) от АБС
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StubSaveDepositABC {

    private final KafkaTemplate<String, ?> kafkaTemplate;
    private final ProductRepository productRepository;
    private final CardRepository cardRepository;

    @Value("${kafka.topics.deposit-service-listener.path}")
    private String topic;


    @KafkaListener(
            topics = "${kafka.topics.deposit-service-producer.path}",
            groupId = "deposit-service",
            containerFactory = "stubListenerFactory")
    public void receiveRequestAndSendAgreement(Message<RequestNewDepositDto> message) {
        log.info("receiveRequestAndSendAgreement() method invoked");
        RequestNewDepositDto requestNewDepositDto = message.getPayload();
        Agreement agreement = setUpAgreement(requestNewDepositDto);
        sendAgreement(agreement);
    }

    private void sendAgreement(Agreement agreement) {
        log.debug("sendAgreement() method invoked");
        log.debug("send Agreement: " + agreement);
        kafkaTemplate.send(
                MessageBuilder
                        .withPayload(agreement)
                        .setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                        .setHeader(KafkaHeaders.TOPIC, topic)
                        .build());
    }


    private Agreement setUpAgreement(RequestNewDepositDto requestNewDepositDto) {
        log.debug("setUpAgreement() method invoke");
        Integer productId = requestNewDepositDto.getProductId();
        BigDecimal initialAmount = requestNewDepositDto.getInitialAmount();
        String cardNumber = requestNewDepositDto.getCardNumber();
        Boolean autoRenewal = requestNewDepositDto.getAutoRenewal();
        BigDecimal interestRate = requestNewDepositDto.getInterestRate();
        Integer durationMonth = requestNewDepositDto.getDurationMonth();

        return Agreement.builder()
                .number(generateNumber())
                .interestRate(interestRate)
                .startDate(setUpStartDate())
                .endDate(setUpEndDate(durationMonth))
                .initialAmount(initialAmount)
                .currentBalance(initialAmount)
                .isActive(true)
                .autoRenewal(autoRenewal)
                .account(getAccount(cardNumber))
                .product(getProduct(productId))
                .build();
    }

    private String generateNumber() {
        log.debug("generateId() method invoke");
        return RandomString.make(20);
    }

    private Product getProduct(Integer productId) {
        log.debug("getProduct() method invoked");
        return productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("product not found"));
    }

    private Account getAccount(String cardNumber) {
        log.debug("getAccount() method invoked");
        Card card = getCard(cardNumber);
        return card.getAccount();
    }

    private Card getCard(String cardNumber) {
        log.debug("getCard() method invoked");
        return cardRepository.findByCardNumber(cardNumber)
                .orElseThrow(() -> new EntityNotFoundException("card with card number " + cardNumber + " not found"));
    }

    private LocalDateTime setUpEndDate(Integer durationMonth) {
        log.debug("setUpEndDate() method invoke");
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusMonths(durationMonth);
        endDate = endDate.plusDays(1L);
        return endDate;
    }

    private LocalDateTime setUpStartDate() {
        log.debug("setUpStartDate() method invoke");
        return LocalDateTime.now();
    }
}
