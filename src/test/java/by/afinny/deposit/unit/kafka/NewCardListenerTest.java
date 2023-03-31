package by.afinny.deposit.unit.kafka;

import by.afinny.deposit.dto.kafka.ConsumerNewCardEvent;
import by.afinny.deposit.integration.NewCardListener;
import by.afinny.deposit.service.OrderService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.test.context.ActiveProfiles;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class NewCardListenerTest {

    @InjectMocks
    private NewCardListener newCardListener;

    @Mock
    private OrderService orderService;

    private ConsumerNewCardEvent event;

    @BeforeAll
    void setUp() {
        event = ConsumerNewCardEvent.builder().build();
    }

    @Test
    @DisplayName("Verify received message")
    void onRequestInsertNewCard() {
        //ACT
        newCardListener.onRequestInsertNewCard(new GenericMessage<>(event));

        //VERIFY
        verify(orderService, times(1)).createNewCard(event);
    }
}