package by.afinny.deposit.unit.kafka;

import by.afinny.deposit.dto.RequestNewDepositDto;
import by.afinny.deposit.integration.DepositSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class DepositSourceTest {

    @Autowired
    private DepositSource source;

    @MockBean
    private KafkaTemplate<String, ?> kafkaTemplate;

    @Value("${kafka.topics.deposit-service-producer.path}")
    private String KAFKA_TOPIC;
    private RequestNewDepositDto requestNewDepositDto;

    @BeforeAll
    void setUp() {
        requestNewDepositDto = RequestNewDepositDto.builder()
                .productId(1)
                .initialAmount(new BigDecimal(10))
                .cardNumber("1")
                .autoRenewal(true)
                .interestRate(new BigDecimal(10))
                .durationMonth(10)
                .build();
    }

    @Test
    @DisplayName("verify sending message to kafka broker")
    void sendMessageAboutDeposit() {
        //ARRANGE
        ArgumentCaptor<Message<?>> messageCaptor = ArgumentCaptor.forClass(Message.class);

        //ACT
        source.sendMessageAboutDeposit(requestNewDepositDto);

        //VERIFY
        verify(kafkaTemplate).send(messageCaptor.capture());
        Message<?> message = messageCaptor.getValue();

        assertThat(message.getPayload()).isEqualTo(requestNewDepositDto);
        assertThat(message.getHeaders()).containsEntry(KafkaHeaders.TOPIC, KAFKA_TOPIC);
    }
}