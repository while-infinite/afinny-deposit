package by.afinny.deposit.unit.mapper;

import by.afinny.deposit.dto.kafka.ConsumerWithdrawEvent;
import by.afinny.deposit.entity.Operation;
import by.afinny.deposit.entity.OperationType;
import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.mapper.OperationMapperImpl;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@DisplayName("OperationMapperTest")
class OperationMapperTest {

    @InjectMocks
    OperationMapperImpl operationMapper;

    private ConsumerWithdrawEvent consumerWithdrawEvent;
    private Operation operation;

    @BeforeEach
    void setUp() {
        consumerWithdrawEvent = consumerWithdrawEvent.builder()
                .completedAt(LocalDateTime.now().toString())
                .sum(BigDecimal.valueOf(1000))
                .currencyCode(CurrencyCode.RUB)
                .type(OperationType.builder()
                        .id(1)
                        .type("REPLENISHMENT")
                        .debit(Boolean.TRUE).build())
                .build();

        operation = operationMapper.consumerWithdrawEventToOperation(consumerWithdrawEvent);
    }

    @Test
    @DisplayName("Verification of correct data generation")
    void toOperation_checkCorrectMappingData(){
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(operation.getCompletedAt())
                .withFailMessage("Сompleted dates should be equals")
                .isEqualTo(consumerWithdrawEvent.getCompletedAt());
        softAssertions.assertThat(operation.getSum())
                .withFailMessage("Sums should be equals")
                .isEqualTo(consumerWithdrawEvent.getSum());
        softAssertions.assertThat(operation.getCurrencyCode())
                .withFailMessage("Currency codes should be equals")
                .isEqualTo(consumerWithdrawEvent.getCurrencyCode());
        softAssertions.assertThat(operation.getType())
                .withFailMessage("Operation types should be equals")
                .isEqualTo(consumerWithdrawEvent.getType());
        softAssertions.assertAll();
    }
}