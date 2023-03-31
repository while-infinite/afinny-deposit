package by.afinny.deposit.unit.mapper;

import by.afinny.deposit.dto.DepositDto;
import by.afinny.deposit.dto.ActiveDepositDto;
import by.afinny.deposit.dto.RefillDebitCardDto;
import by.afinny.deposit.entity.Account;
import by.afinny.deposit.entity.Agreement;
import by.afinny.deposit.entity.Card;
import by.afinny.deposit.entity.Operation;
import by.afinny.deposit.entity.Product;
import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.entity.constant.SchemaName;
import by.afinny.deposit.mapper.DepositMapperImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class DepositMapperTest {

    @InjectMocks
    private DepositMapperImpl depositMapper;

    private Card card;
    private Agreement agreement;
    private Product product;
    private DepositDto depositDto;
    private Agreement agr;
    private Card defaultCard;
    private List<Agreement> agreements;
    private List<ActiveDepositDto> activeDepositDtoList;
    private Account account;
    private RefillDebitCardDto refillDebitCardDto;
    private Operation operation;

    @BeforeAll
    void setUp() {
        card = Card.builder()
                .cardNumber("1234567890123456").build();
        agreement = Agreement.builder()
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMonths(12L))
                .interestRate(new BigDecimal(10))
                .currentBalance(new BigDecimal(10))
                .autoRenewal(true).build();
        product = Product.builder()
                .name("name")
                .currencyCode(CurrencyCode.RUB)
                .schemaName(SchemaName.FIXED)
                .isCapitalization(true)
                .isRevocable(true).build();

        defaultCard = Card.builder()
                .cardNumber("1111222233334444")
                .isDefault(true)
                .build();
        Card notDefaultCard = Card.builder()
                .cardNumber("4444333322221111")
                .isDefault(false)
                .build();
        agr = Agreement.builder()
                .id(UUID.randomUUID())
                .number("number")
                .interestRate(new BigDecimal("10.00"))
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .initialAmount(new BigDecimal("1000000.00"))
                .currentBalance(new BigDecimal("10000.00"))
                .isActive(true)
                .account(Account.builder()
                        .accountNumber("accountNumber")
                        .cards(List.of(defaultCard, notDefaultCard))
                        .build())
                .product(Product.builder()
                        .name("product test name")
                        .currencyCode(CurrencyCode.RUB)
                        .build())
                .autoRenewal(true).build();
        agreements = List.of(agr);
        account = Account.builder()
                .accountNumber("123")
                .build();
        refillDebitCardDto = RefillDebitCardDto.builder()
                .sum("100")
                .accountNumber("123")
                .build();
    }

    @Test
    @DisplayName("Verify deposit dto fields")
    void toDepositDto_shouldReturnDepositDto() {
        //ACT
        depositDto = depositMapper.toDepositDto(agreement, product, card);
        //VERIFY
        verifyCardFields();
        verifyAgreementFields();
        verifyProductFields();
    }

    @Test
    @DisplayName("Verify deposit dto fields setting")
    void toDepositsDto_shouldReturnCorrectMappingData() {
        //ACT
        activeDepositDtoList = depositMapper.toActiveDepositsDto(agreements);
        //VERIFY
        verifyProductField();
    }

    @Test
    @DisplayName("Check fields RefillDebitCardDto and Operation are equals")
    void toOperation_checkCorrectMappingData(){
        //ACT
        operation = depositMapper.refillDebitCardDtoToOperation(refillDebitCardDto, account, LocalDateTime.now());

        //VERIFY
        verifyOperationFields();
    }

    private void verifyProductField() {
        ActiveDepositDto resultActiveDepositDto = activeDepositDtoList.get(0);
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(resultActiveDepositDto.getAgreementId()).isEqualTo(agr.getId());
            softAssertions.assertThat(resultActiveDepositDto.getStartDate()).isEqualTo(agr.getStartDate());
            softAssertions.assertThat(resultActiveDepositDto.getEndDate()).isEqualTo(agr.getEndDate());
            softAssertions.assertThat(resultActiveDepositDto.getCurrentBalance()).isEqualTo(agr.getCurrentBalance());
            softAssertions.assertThat(resultActiveDepositDto.getProductName()).isEqualTo(agr.getProduct().getName());
            softAssertions.assertThat(resultActiveDepositDto.getCurrencyCode()).isEqualTo(agr.getProduct().getCurrencyCode());
            softAssertions.assertThat(resultActiveDepositDto.getCardNumber()).isEqualTo(defaultCard.getCardNumber());
        });
    }

    private void verifyCardFields() {
        assertSoftly(softAssertions -> softAssertions.assertThat(depositDto.getCardNumber()).isEqualTo(card.getCardNumber()));
    }

    private void verifyAgreementFields() {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(depositDto.getStartDate()).isEqualTo(agreement.getStartDate());
            softAssertions.assertThat(depositDto.getEndDate()).isEqualTo(agreement.getEndDate());
            softAssertions.assertThat(depositDto.getInterestRate()).isEqualTo(agreement.getInterestRate());
            softAssertions.assertThat(depositDto.getCurrentBalance()).isEqualTo(agreement.getCurrentBalance());
            softAssertions.assertThat(depositDto.getAutoRenewal()).isEqualTo(agreement.getAutoRenewal());
        });
    }

    private void verifyProductFields() {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(depositDto.getName()).isEqualTo(product.getName());
            softAssertions.assertThat(depositDto.getCurrencyCode()).isEqualTo(product.getCurrencyCode());
            softAssertions.assertThat(depositDto.getSchemaName()).isEqualTo(product.getSchemaName());
            softAssertions.assertThat(depositDto.getIsCapitalization()).isEqualTo(product.getIsCapitalization());
            softAssertions.assertThat(depositDto.getIsRevocable()).isEqualTo(product.getIsRevocable());
        });
    }

    private void verifyOperationFields() {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(operation.getSum()).isEqualTo(refillDebitCardDto.getSum());
            softAssertions.assertThat(operation.getAccount().getAccountNumber()).isEqualTo(refillDebitCardDto.getAccountNumber());
        });
    }
}
