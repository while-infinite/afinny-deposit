package by.afinny.deposit.unit.kafka;

import by.afinny.deposit.entity.Agreement;
import by.afinny.deposit.integration.DepositTopicListener;
import by.afinny.deposit.service.DepositService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class DepositTopicListenerTest {

    @InjectMocks
    private DepositTopicListener depositTopicListener;

    @Mock
    private DepositService depositService;

    private Agreement agreement;

    @BeforeAll
    void setUp() {
        agreement = Agreement.builder().build();
    }

    @Test
    @DisplayName("Verify received message")
    void receiveDeposit_shouldInvokeSaveAgreement() {
        //ACT
        depositTopicListener.receiveDeposit(new GenericMessage<>(agreement));

        //VERIFY
        verify(depositService, times(1)).saveAgreement(any(Agreement.class));
    }
}