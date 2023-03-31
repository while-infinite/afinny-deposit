package by.afinny.deposit.unit.kafka;

import by.afinny.deposit.dto.NewPinCodeDebitCardDto;
import by.afinny.deposit.integration.NewPinCodeDebitCardSource;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
public class NewPinCodeDebitCardSourceTest {
    @Autowired
    private NewPinCodeDebitCardSource source;

    @MockBean
    private KafkaTemplate<String, ?> kafkaTemplate;

    @Value("${kafka.topics.new-pin-code-card-producer.path}")
    private String KAFKA_TOPIC;
    private NewPinCodeDebitCardDto newPinCodeDebitCardDto;

    @BeforeAll
    void setUp() {
        newPinCodeDebitCardDto = NewPinCodeDebitCardDto.builder()
                .cardNumber("545465")
                .newPin("7879")
                .build();
    }

    @Test
    @DisplayName("verify sending message to kafka broker")
    void sendMessageAboutNewPinCodeCard() {
        //ARRANGE
        ArgumentCaptor<Message<?>> messageCaptor = ArgumentCaptor.forClass(Message.class);

        //ACT
        source.sendMessageAboutNewPinCodeCard(newPinCodeDebitCardDto);

        //VERIFY
        verify(kafkaTemplate).send(messageCaptor.capture());
        Message<?> message = messageCaptor.getValue();

        assertThat(message.getPayload()).isEqualTo(newPinCodeDebitCardDto);
        assertThat(message.getHeaders()).containsEntry(KafkaHeaders.TOPIC, KAFKA_TOPIC);
    }
}

