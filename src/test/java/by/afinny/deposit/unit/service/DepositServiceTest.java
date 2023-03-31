package by.afinny.deposit.unit.service;

import by.afinny.deposit.dto.ActiveDepositDto;
import by.afinny.deposit.dto.DepositDto;
import by.afinny.deposit.dto.RefillDebitCardDto;
import by.afinny.deposit.dto.RequestNewDepositDto;
import by.afinny.deposit.entity.Account;
import by.afinny.deposit.entity.Agreement;
import by.afinny.deposit.entity.Card;
import by.afinny.deposit.entity.CardProduct;
import by.afinny.deposit.entity.Operation;
import by.afinny.deposit.entity.Product;
import by.afinny.deposit.entity.constant.CardStatus;
import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.entity.constant.DigitalWallet;
import by.afinny.deposit.exception.EntityNotFoundException;
import by.afinny.deposit.mapper.DepositMapperImpl;
import by.afinny.deposit.repository.AccountRepository;
import by.afinny.deposit.repository.AgreementRepository;
import by.afinny.deposit.repository.CardRepository;
import by.afinny.deposit.repository.OperationRepository;
import by.afinny.deposit.repository.ProductRepository;
import by.afinny.deposit.service.impl.DepositServiceImpl;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_METHOD;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(PER_METHOD)
@ActiveProfiles("test")
class DepositServiceTest {

    @InjectMocks
    private DepositServiceImpl depositService;

    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private AgreementRepository agreementRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private DepositMapperImpl depositMapper;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private OperationRepository operationRepository;

    private final UUID CLIENT_ID = UUID.randomUUID();
    private final UUID AGREEMENT_ID = UUID.randomUUID();
    private final UUID CARD_ID = UUID.randomUUID();
    private final Integer PRODUCT_ID = 123456;
    private final String CARD_NUMBER = "1234-5678-9876-5432";
    private List<Agreement> agreementList;
    private List<ActiveDepositDto> activeDepositDtoList;
    private ActiveDepositDto activeDepositDto;
    private Card card;
    private Product product;
    private RequestNewDepositDto requestNewDepositDto;
    private Agreement agreement;
    private DepositDto depositDto;
    private RefillDebitCardDto refillDebitCardDto;
    private Account account;
    private Operation operation;

    @BeforeEach
    void setUp() {
        activeDepositDto = ActiveDepositDto.builder()
                .agreementId(UUID.randomUUID())
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusYears(1))
                .currentBalance(new BigDecimal(100))
                .productName("product name")
                .currencyCode(CurrencyCode.RUB)
                .cardNumber("1111222233334444").build();
        agreementList = List.of(
                Agreement.builder()
                        .id(UUID.randomUUID())
                        .startDate(LocalDateTime.now())
                        .endDate(LocalDateTime.now().plusDays(1))
                        .currentBalance(new BigDecimal("10000.00"))
                        .isActive(true)
                        .account(Account.builder()
                                .clientId(CLIENT_ID).build())
                        .product(Product.builder()
                                .name("product name")
                                .currencyCode(CurrencyCode.RUB)
                                .build())
                        .build());
        activeDepositDtoList = List.of(activeDepositDto, activeDepositDto);
        card = Card.builder()
                .id(UUID.randomUUID())
                .cardNumber(CARD_NUMBER)
                .transactionLimit(new BigDecimal("100"))
                .expirationDate(LocalDate.now().plusDays(1))
                .holderName("Peter Parker")
                .status(CardStatus.ACTIVE)
                .digitalWallet(DigitalWallet.APPLEPAY)
                .isDefault(true)
                .account(Account.builder()
                        .clientId(CLIENT_ID).build())
                .cardProduct(CardProduct.builder()
                        .id(1)
                        .build())
                .build();
        product = Product.builder()
                .id(PRODUCT_ID)
                .name("product")
                .interestRateEarly(new BigDecimal("100"))
                .isCapitalization(true)
                .amountMin(new BigDecimal("1"))
                .amountMax(new BigDecimal("1000"))
                .isActive(true)
                .isRevocable(true)
                .currencyCode(CurrencyCode.RUB)
                .agreement(agreementList)
                .build();
        requestNewDepositDto = RequestNewDepositDto.builder()
                .productId(123456)
                .initialAmount(new BigDecimal("100"))
                .cardNumber(CARD_NUMBER)
                .autoRenewal(true)
                .interestRate(new BigDecimal("100"))
                .durationMonth(12)
                .build();
        agreement = Agreement.builder()
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMonths(12L))
                .interestRate(new BigDecimal(10))
                .currentBalance(new BigDecimal(10))
                .autoRenewal(true)
                .product(product).build();
        depositDto = DepositDto.builder()
                .cardNumber(CARD_NUMBER)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusYears(1))
                .interestRate(new BigDecimal("100"))
                .currentBalance(new BigDecimal("100"))
                .autoRenewal(true)
                .name("name")
                .currencyCode(CurrencyCode.RUB)
                .isCapitalization(true)
                .isRevocable(true)
                .build();
        refillDebitCardDto = RefillDebitCardDto.builder()
                .sum("100")
                .accountNumber("123")
                .build();
        account = Account.builder()
                .id(CLIENT_ID)
                .accountNumber("123")
                .build();
        operation = Operation.builder()
                .id(CLIENT_ID)
                .account(account)
                .build();
    }

    @Test
    @DisplayName("Return available deposits when active deposit products exists")
    void getActiveDeposits_shouldReturnListProducts() {
        //ARRANGE
        when(agreementRepository.findByAccountClientIdAndIsActiveTrue(CLIENT_ID)).thenReturn(agreementList);
        when(depositMapper.toActiveDepositsDto(agreementList)).thenReturn(activeDepositDtoList);

        //ACT
        List<ActiveDepositDto> resultActiveDepositDtoList = depositService.getActiveDeposits(CLIENT_ID);

        //VERIFY
        verifyProductDtoFields(resultActiveDepositDtoList);
    }

    @Test
    @DisplayName("If not success then throw Runtime Exception")
    void getActiveDeposits_ifNotSuccess_thenThrow() {
        //ARRANGE
        when(agreementRepository.findByAccountClientIdAndIsActiveTrue(CLIENT_ID)).thenThrow(RuntimeException.class);
        //ACT
        ThrowableAssert.ThrowingCallable getActiveDepositProductsMethod = () -> depositService.getActiveDeposits(CLIENT_ID);
        //VERIFY
        assertThatThrownBy(getActiveDepositProductsMethod).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("if card number and product id exist then send to kafka")
    void createNewDeposit_shouldSendToKafka() {
        //ARRANGE
        when(cardRepository.findByAccountClientIdAndCardNumber(CLIENT_ID, CARD_NUMBER)).thenReturn(Optional.of(card));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        //ACT
        depositService.createNewDeposit(CLIENT_ID, requestNewDepositDto);

        //VERIFY
        verify(eventPublisher).publishEvent(requestNewDepositDto);
    }

    @Test
    @DisplayName("if card number doesn't exist then throw")
    void createNewDeposit_ifCardNumberDoesNotExistThenThrow() {
        //ARRANGE
        when(cardRepository.findByAccountClientIdAndCardNumber(CLIENT_ID, CARD_NUMBER)).thenReturn(Optional.empty());
        lenient().when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        //ACT
        ThrowableAssert.ThrowingCallable createNewDepositMethod = () -> depositService.createNewDeposit(CLIENT_ID, requestNewDepositDto);

        //VERIFY
        verify(eventPublisher, never()).publishEvent(requestNewDepositDto);
        assertThatThrownBy(createNewDepositMethod).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("if product id doesn't exist then throw")
    void createNewDeposit_ifProductIdDoesNotExistThenThrow() {
        //ARRANGE
        when(cardRepository.findByAccountClientIdAndCardNumber(CLIENT_ID, CARD_NUMBER)).thenReturn(Optional.of(card));
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());

        //ACT
        ThrowableAssert.ThrowingCallable createNewDepositMethod = () -> depositService.createNewDeposit(CLIENT_ID, requestNewDepositDto);

        //VERIFY
        verify(eventPublisher, never()).publishEvent(requestNewDepositDto);
        assertThatThrownBy(createNewDepositMethod).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("if received agreement successfully then save agreement")
    void saveAgreement_shouldSaveAgreement() {
        //ACT
        depositService.saveAgreement(new Agreement());

        //VERIFY
        verify(agreementRepository, times(1)).save(any(Agreement.class));
    }

    @Test
    @DisplayName("if agreement and card were found return deposit dto")
    void getDeposit_shouldReturnDepositDto() {
        //ARRANGE
        when(agreementRepository.findByAccountClientIdAndId(CLIENT_ID, AGREEMENT_ID)).thenReturn(Optional.of(agreement));
        when(cardRepository.findById(CARD_ID)).thenReturn(Optional.of(card));
        when(depositMapper.toDepositDto(agreement, product, card)).thenReturn(depositDto);

        //ACT
        DepositDto result = depositService.getDeposit(CLIENT_ID, AGREEMENT_ID, CARD_ID);

        //VERIFY
        assertThat(result).isNotNull();
        verifyDepositDto(depositDto, result);
    }

    @Test
    @DisplayName("if agreement was not found return throw EntityNotFoundException")
    void getDeposit_ifAgreementNotFound_thenThrow() {
        //ARRANGE
        when(agreementRepository.findByAccountClientIdAndId(any(), any())).thenThrow(javax.persistence.EntityNotFoundException.class);

        //ACT
        ThrowableAssert.ThrowingCallable getDepositMethodInvocation = () -> depositService.
                getDeposit(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID());

        //VERIFY
        assertThatThrownBy(getDepositMethodInvocation).isInstanceOf(javax.persistence.EntityNotFoundException.class);
    }

    @Test
    @DisplayName("If account were found, create and save new operation")
    void refillUserDebitCard_shouldSaveOperation() {
        //ARRANGE
        when(accountRepository.findByAccountNumber(any())).thenReturn(Optional.ofNullable(account));
        when(depositMapper.refillDebitCardDtoToOperation(eq(refillDebitCardDto), eq(account), any(LocalDateTime.class)))
                .thenReturn(operation);

        //ACT
        depositService.refillUserDebitCard(CLIENT_ID, refillDebitCardDto);

        //VERIFY
        verify(operationRepository, times(1)).save(operation);
    }

    @Test
    @DisplayName("If account was not found then throw EntityNotFoundException")
    void refillUserDebitCard_ifAccountNotFound_thenThrow() {
        //ARRANGE
        when(accountRepository.findByAccountNumber(any())).thenThrow(EntityNotFoundException.class);

        //ACT
        ThrowableAssert.ThrowingCallable getAccountMethodInvocation =
                () -> depositService.refillUserDebitCard(UUID.randomUUID(), refillDebitCardDto);

        //VERIFY
        verify(depositMapper, never()).refillDebitCardDtoToOperation(any(RefillDebitCardDto.class), any(Account.class), any(LocalDateTime.class));
        assertThatThrownBy(getAccountMethodInvocation).isInstanceOf(EntityNotFoundException.class);
    }

    private void verifyDepositDto(DepositDto expected, DepositDto actual) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(actual.getCardNumber())
                    .isEqualTo(expected.getCardNumber());
            softAssertions.assertThat(actual.getStartDate())
                    .isEqualTo(expected.getStartDate());
            softAssertions.assertThat(actual.getEndDate())
                    .isEqualTo(expected.getEndDate());
            softAssertions.assertThat(actual.getInterestRate())
                    .isEqualTo(expected.getInterestRate());
            softAssertions.assertThat(actual.getCurrentBalance())
                    .isEqualTo(expected.getCurrentBalance());
            softAssertions.assertThat(actual.getAutoRenewal())
                    .isEqualTo(expected.getAutoRenewal());
            softAssertions.assertThat(actual.getName())
                    .isEqualTo(expected.getName());
            softAssertions.assertThat(actual.getCurrencyCode())
                    .isEqualTo(expected.getCurrencyCode());
            softAssertions.assertThat(actual.getSchemaName())
                    .isEqualTo(expected.getSchemaName());
            softAssertions.assertThat(actual.getIsCapitalization())
                    .isEqualTo(expected.getIsCapitalization());
            softAssertions.assertThat(actual.getIsRevocable())
                    .isEqualTo(expected.getIsRevocable());
        });
    }

    private void verifyProductDtoFields(List<ActiveDepositDto> activeDepositDtoList) {
        ActiveDepositDto resultActiveDepositDto = activeDepositDtoList.get(0);
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(activeDepositDtoList)
                    .withFailMessage("List of depositDto shouldn't be null")
                    .isNotNull();
            softAssertions.assertThat(resultActiveDepositDto.getAgreementId())
                    .withFailMessage("Agreement Id should be equals")
                    .isEqualTo(activeDepositDto.getAgreementId());
            softAssertions.assertThat(resultActiveDepositDto.getStartDate())
                    .withFailMessage("Start date should be equals")
                    .isEqualTo(activeDepositDto.getStartDate());
            softAssertions.assertThat(resultActiveDepositDto.getEndDate())
                    .withFailMessage("End date should be equals")
                    .isEqualTo(activeDepositDto.getEndDate());
            softAssertions.assertThat(resultActiveDepositDto.getCurrentBalance())
                    .withFailMessage("Current balance should be equals")
                    .isEqualTo(activeDepositDto.getCurrentBalance());
            softAssertions.assertThat(resultActiveDepositDto.getProductName())
                    .withFailMessage("Product name should be equals")
                    .isEqualTo(activeDepositDto.getProductName());
            softAssertions.assertThat(resultActiveDepositDto.getCurrencyCode())
                    .withFailMessage("Currency code should be equals")
                    .isEqualTo(activeDepositDto.getCurrencyCode());
            softAssertions.assertThat(resultActiveDepositDto.getCardNumber())
                    .withFailMessage("Card number should be equals")
                    .isEqualTo(activeDepositDto.getCardNumber());
        });
    }
}