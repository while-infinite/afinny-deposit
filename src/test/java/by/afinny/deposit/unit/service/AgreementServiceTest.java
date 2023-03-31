package by.afinny.deposit.unit.service;

import by.afinny.deposit.dto.AutoRenewalDto;
import by.afinny.deposit.dto.kafka.ConsumerWithdrawEvent;
import by.afinny.deposit.entity.Account;
import by.afinny.deposit.entity.Agreement;
import by.afinny.deposit.entity.Operation;
import by.afinny.deposit.entity.OperationType;
import by.afinny.deposit.entity.Product;
import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.exception.EntityNotFoundException;
import by.afinny.deposit.mapper.OperationMapper;
import by.afinny.deposit.repository.AccountRepository;
import by.afinny.deposit.repository.AgreementRepository;
import by.afinny.deposit.repository.OperationRepository;
import by.afinny.deposit.service.impl.AgreementServiceImpl;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ActiveProfiles("test")
class AgreementServiceTest {

    @InjectMocks
    private AgreementServiceImpl agreementService;
    @Mock
    private AgreementRepository agreementRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private OperationMapper operationMapper;
    @Mock
    private OperationRepository operationRepository;
    @Spy
    private ApplicationEventPublisher eventPublisher;

    private final UUID agreementId = UUID.randomUUID();
    private final UUID clientId = UUID.randomUUID();

    private Agreement agreement;
    private Account account;
    private Product product;
    private Operation operation;
    private ConsumerWithdrawEvent consumerWithdrawEvent;
    private AutoRenewalDto autoRenewalDto;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .interestRateEarly(BigDecimal.valueOf(3)).build();
        account = Account.builder()
                .accountNumber("013459876")
                .currentBalance(BigDecimal.valueOf(15000))
                .build();
        agreement = Agreement.builder()
                .id(agreementId)
                .isActive(Boolean.TRUE)
                .product(product)
                .currentBalance(BigDecimal.valueOf(500))
                .account(account).build();
        operation = Operation.builder().build();
        consumerWithdrawEvent = ConsumerWithdrawEvent.builder()
                .agreementId(agreementId)
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
        autoRenewalDto = AutoRenewalDto.builder()
                .autoRenewal(true)
                .build();
    }
    @Test
    @DisplayName("if agreement with incoming id was found then revoke")
    void getAgreement_ShouldBeRevoked() {
        //ARRANGE
        when(agreementRepository.findById(any(UUID.class))).thenReturn(Optional.of(agreement));
        when(accountRepository.findByAccountNumber(any(String.class))).thenReturn(Optional.of(account));
        when(operationMapper.consumerWithdrawEventToOperation(any(ConsumerWithdrawEvent.class))).thenReturn(operation);

        //ACT
        agreementService.modifyAgreementAndCreateOperation(consumerWithdrawEvent);

        //VERIFY
        assertThat(agreement.getIsActive()).isEqualTo(Boolean.FALSE);
    }

    @Test
    @DisplayName("if agreement with incoming id wasn't found then throw EntityNotFoundException")
    void getAgreement_ifAgreementNotFound_thenThrow() {
        //ARRANGE
        when(agreementRepository.findById(any(UUID.class))).thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumber(any(String.class))).thenReturn(Optional.of(account));

        //ACT
        ThrowingCallable modifyAgreementAndInsertOperationMethodInvocation = () -> agreementService
                .modifyAgreementAndCreateOperation(consumerWithdrawEvent);

        //VERIFY
        assertThatThrownBy(modifyAgreementAndInsertOperationMethodInvocation)
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("if agreement with incoming id wasn't found then throw EntityNotFoundException")
    void getAgreement_ifAccountNotFound_thenThrow() {
        //ARRANGE
        when(accountRepository.findByAccountNumber(any(String.class))).thenThrow(EntityNotFoundException.class);

        //ACT
        ThrowingCallable modifyAgreementAndInsertOperationMethodInvocation = () -> agreementService
                .modifyAgreementAndCreateOperation(consumerWithdrawEvent);

        //VERIFY
        assertThatThrownBy(modifyAgreementAndInsertOperationMethodInvocation)
                .isInstanceOf(RuntimeException.class);
    }


    @Test
    @DisplayName("if agreement with incoming id wasn't found then save")
    void updateAutoRenewal_shouldUpdateAutoRenewal() {
        //ARRANGE
        when(agreementRepository.findAgreementByAccountClientIdAndIdAndIsActiveTrue(clientId, agreementId)).thenReturn(Optional.of(agreement));
        //ACT
        agreementService.updateAutoRenewal(clientId, agreementId, autoRenewalDto);
        //VERIFY
        verify(agreementRepository).save(agreement);
    }

    @Test
    @DisplayName("if agreement with incoming id wasn't found then throw")
    void updateAutoRenewal_ifIsActiveFalse_thenThrow() {
        //ARRANGE
        when(agreementRepository.findAgreementByAccountClientIdAndIdAndIsActiveTrue(any(UUID.class), any(UUID.class))).thenReturn(Optional.empty());
        //ACT
        ThrowingCallable updateAutoRenewal = () -> agreementService.updateAutoRenewal(clientId, agreementId, autoRenewalDto);
        //VERIFY
        assertThatThrownBy(updateAutoRenewal).isInstanceOf(EntityNotFoundException.class);
        verify(agreementRepository, never()).save(agreement);
    }
}