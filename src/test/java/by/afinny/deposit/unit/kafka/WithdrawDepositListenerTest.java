package by.afinny.deposit.unit.kafka;

import by.afinny.deposit.dto.kafka.ConsumerWithdrawEvent;
import by.afinny.deposit.entity.OperationType;
import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.integration.WithdrawDepositListener;
import by.afinny.deposit.service.AgreementService;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ActiveProfiles("test")
class WithdrawDepositListenerTest {

    @Mock
    private AgreementService agreementService;

    @InjectMocks
    private WithdrawDepositListener withdrawDepositListener;

    private ConsumerWithdrawEvent event;

    @BeforeEach
    void setUp() {
        event = ConsumerWithdrawEvent.builder()
                .agreementId(UUID.randomUUID())
                .accountNumber("013459876")
                .isActive(Boolean.FALSE)
                .currentBalance(BigDecimal.valueOf(0))
                .completedAt(LocalDateTime.now().minusMinutes(10).toString())
                .sum(BigDecimal.valueOf(1500))
                .currencyCode(CurrencyCode.RUB)
                .type(OperationType.builder()
                        .id(1)
                        .type("REPLENISHMENT")
                        .debit(true).build())
                .build();
    }

    @Test
    @DisplayName("Verify received message")
    void onRequestUpdateAgreementStatusAndInsertOperationEvent_shouldInvokeModifyAgreementAndInsertOperation() {
        //ARRANGE
        ArgumentCaptor<ConsumerWithdrawEvent> consumerWithdrawEventArgumentCaptor
                = ArgumentCaptor.forClass(ConsumerWithdrawEvent.class);

        //ACT
        withdrawDepositListener.onRequestUpdateAgreementStatusAndInsertOperationEvent(new GenericMessage<>(event));

        //VERIFY
        verify(agreementService).modifyAgreementAndCreateOperation(consumerWithdrawEventArgumentCaptor.capture());
        verifyEvent(consumerWithdrawEventArgumentCaptor.getValue());
    }

    private void verifyEvent(ConsumerWithdrawEvent consumerWithdrawEvent) {
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(consumerWithdrawEvent.getAccountNumber())
                .withFailMessage("Account numbers should be equals")
                .isEqualTo(event.getAccountNumber());
        softAssertions.assertThat(consumerWithdrawEvent.getAgreementId())
                .withFailMessage("Agreement ids should be equals")
                .isEqualTo(event.getAgreementId());
        softAssertions.assertThat(consumerWithdrawEvent.getIsActive())
                .withFailMessage("IsActive statuses should be equals")
                .isEqualTo(event.getIsActive());
        softAssertions.assertThat(consumerWithdrawEvent.getCurrentBalance())
                .withFailMessage("Current balances should be equals")
                .isEqualTo(event.getCurrentBalance());
        softAssertions.assertThat(consumerWithdrawEvent.getCompletedAt())
                .withFailMessage("Сompleted dates should be equals")
                .isEqualTo(event.getCompletedAt());
        softAssertions.assertThat(consumerWithdrawEvent.getSum())
                .withFailMessage("Sums should be equals")
                .isEqualTo(event.getSum());
        softAssertions.assertThat(consumerWithdrawEvent.getCurrencyCode())
                .withFailMessage("Currency codes should be equals")
                .isEqualTo(event.getCurrencyCode());
        softAssertions.assertThat(consumerWithdrawEvent.getType())
                .withFailMessage("Operation types should be equals")
                .isEqualTo(event.getType());
        softAssertions.assertAll();
    }
}