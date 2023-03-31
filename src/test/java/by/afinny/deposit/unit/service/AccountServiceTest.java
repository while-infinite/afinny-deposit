package by.afinny.deposit.unit.service;

import by.afinny.deposit.dto.AccountNumberDto;
import by.afinny.deposit.dto.AccountWithCardInfoDto;
import by.afinny.deposit.dto.ViewCardDto;
import by.afinny.deposit.dto.userservice.AccountDto;
import by.afinny.deposit.entity.Account;
import by.afinny.deposit.entity.Card;
import by.afinny.deposit.entity.constant.PaymentSystem;
import by.afinny.deposit.exception.EntityNotFoundException;
import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.mapper.AccountMapper;
import by.afinny.deposit.mapper.CardMapper;
import by.afinny.deposit.repository.AccountRepository;
import by.afinny.deposit.repository.CardRepository;
import by.afinny.deposit.service.impl.AccountServiceImpl;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class AccountServiceTest {

    @InjectMocks
    private AccountServiceImpl accountService;
    @InjectMocks
    private AccountServiceImpl cardService;

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private AccountMapper accountMapper;
    @Mock
    private CardMapper cardMapper;

    private final UUID CLIENT_ID = UUID.randomUUID();
    private final UUID CARD_ID = UUID.randomUUID();
    private List<AccountWithCardInfoDto> activeAccountsWithCardsDto;
    private List<AccountDto> activeAccountsDto;
    private AccountNumberDto accountNumberDto;
    private Card card;
    private ViewCardDto viewCardDto;
    private Account account;

    @BeforeEach
    void setUp() {
        activeAccountsWithCardsDto = new ArrayList<>();
        account = Account.builder()
                .cards(new ArrayList<>())
                .id(CLIENT_ID)
                .clientId(CLIENT_ID)
                .accountNumber("accountNumber")
                .currencyCode(CurrencyCode.RUB)
                .currentBalance(BigDecimal.ONE)
                .closeDate(LocalDate.now().plusYears(2L))
                .openDate(LocalDate.now().minusYears(2L))
                .isActive(true)
                .salaryProject("2")
                .build();

        card = Card.builder()
               .id(CARD_ID)
               .account(account)
               .build();

        viewCardDto = ViewCardDto.builder()
                .cardId(CARD_ID)
                .build();

        accountNumberDto = AccountNumberDto.builder()
               .accountNumber("accountNumber")
               .build();

        activeAccountsWithCardsDto.add(AccountWithCardInfoDto.builder()
                .cardId(CARD_ID)
                .cardNumber("cardNumber")
                .expirationDate(LocalDate.now().plusYears(2L))
                .cardName("cardName")
                .paymentSystem(PaymentSystem.VISA)
                .currencyCode(CurrencyCode.RUB)
                .cardBalance(BigDecimal.ONE)
                .build());

        activeAccountsDto = List.of(AccountDto.builder()
                .accountNumber("accountNumber")
                .clientId(CLIENT_ID)
                .currencyCode(CurrencyCode.RUB)
                .currentBalance(BigDecimal.ONE)
                .isActive(true)
                .salaryProject("2")
                .closeDate(LocalDate.now().plusYears(2L))
                .openDate(LocalDate.now().minusYears(2L))
                .build());
    }

    @Test
    @DisplayName("If success then actual and expected amount of accounts and cards are equals")
    void getActiveAccountsWithCards_ifSuccess_thenReturnListAccounts() {
        //ARRANGE
        when(accountRepository.findByClientIdAndIsActiveTrue(CLIENT_ID)).thenReturn(List.of(account));
        when(accountMapper.toAccountsWithCardsDto(account.getCards())).thenReturn(activeAccountsWithCardsDto);

        //ACT
        List<AccountWithCardInfoDto> result = accountService.getActiveAccountsWithCard(CLIENT_ID);

        //VERIFY
        for(int i = 0; i < account.getCards().size(); i++)
            verifyCard(account.getCards().get(i), result.get(i));
    }

    @Test
    @DisplayName("If not success then throw Runtime Exception")
    void getActiveAccountsWithCards_ifNotSuccess_thenThrow() {
        //ARRANGE
        when(accountRepository.findByClientIdAndIsActiveTrue(CLIENT_ID)).thenThrow(RuntimeException.class);

        //ACT
        ThrowingCallable throwingCallable = () -> accountService.getActiveAccountsWithCard(CLIENT_ID);

        //VERIFY
        assertThatThrownBy(throwingCallable).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("If success then actual and expected amount of accounts are equals")
    void getActiveAccounts_ifSuccess_thenReturnListAccounts() {
        //ARRANGE
        when(accountRepository.findByClientIdAndIsActiveTrue(CLIENT_ID)).thenReturn(List.of(account));
        when(accountMapper.toAccountsDto(List.of(account))).thenReturn(activeAccountsDto);

        //ACT
        List<AccountDto> result = accountService.getActiveAccounts(CLIENT_ID);

        //VERIFY
        verifyAccountCard(account, result.get(0));
    }

    @Test
    @DisplayName("If not success then throw Runtime Exception")
    void getActiveAccounts_ifNotSuccess_thenThrow() {
        //ARRANGE
        when(accountRepository.findByClientIdAndIsActiveTrue(CLIENT_ID)).thenThrow(RuntimeException.class);

        //ACT
        ThrowingCallable getActiveAccountsMethod = () -> accountService.getActiveAccountsWithCard(CLIENT_ID);

        //VERIFY
        assertThatThrownBy(getActiveAccountsMethod).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("if card with incoming card number was found then return accountNumber")
    void getViewCardByCardId_shouldReturnAccountNumberDto(){
        //ARRANGE
        when(cardRepository.findByAccountClientIdAndId(CLIENT_ID, CARD_ID)).thenReturn(Optional.of(card));
        when(cardMapper.toViewCardDto(card)).thenReturn(viewCardDto);

        //ACT
        ViewCardDto result = cardService.getViewCardByCardId(CLIENT_ID, CARD_ID);

        //VERIFY
        assertThat(result).isNotNull();
        assertThat(result.toString()).isEqualTo(viewCardDto.toString());
    }

    @Test
    @DisplayName("if card with incoming card number wasn't found then throws EntityNotFoundException")
    void getAccountByCardId_shouldThrow(){
        //ARRANGE
        when(cardRepository.findByAccountClientIdAndId(CLIENT_ID, CARD_ID)).thenReturn(Optional.empty());

        //ACT
        ThrowingCallable getAccountByCardIdMethodInvocation = ()-> accountService.getViewCardByCardId(CLIENT_ID, CARD_ID);

        //VERIFY
        assertThatThrownBy(getAccountByCardIdMethodInvocation).isInstanceOf(EntityNotFoundException.class);
    }

    void verifyCard(Card card, AccountWithCardInfoDto accountDto) {
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(card.getId())
                .withFailMessage("AccountId should be equals")
                .isEqualTo(accountDto.getCardId());
        softAssertions.assertThat(card.getCardNumber())
                .withFailMessage("CardNumber should be equals")
                .isEqualTo(accountDto.getCardNumber());
        softAssertions.assertThat(card.getExpirationDate())
                .withFailMessage("ExpirationDate should be equals")
                .isEqualTo(accountDto.getExpirationDate());
        softAssertions.assertThat(card.getCardProduct().getCardName())
                .withFailMessage("CardName should be equals")
                .isEqualTo(accountDto.getCardName());
        softAssertions.assertThat(card.getCardProduct().getPaymentSystem())
                .withFailMessage("PaymentSystem should be equals")
                .isEqualTo(accountDto.getPaymentSystem());
        softAssertions.assertThat(account.getCurrentBalance())
                .withFailMessage("CurrentBalance should be equals")
                .isEqualTo(accountDto.getCardBalance());
        softAssertions.assertThat(account.getCurrencyCode())
                .withFailMessage("CurrencyCode should be equals")
                .isEqualTo(accountDto.getCurrencyCode());
        softAssertions.assertAll();
    }

    void verifyAccountCard(Account account, AccountDto accountDto) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(account.getAccountNumber())
                    .withFailMessage("AccountNumber should be equals")
                    .isEqualTo(accountDto.getAccountNumber());
            softAssertions.assertThat(account.getClientId())
                    .withFailMessage("ClientId should be equals")
                    .isEqualTo(accountDto.getClientId());
            softAssertions.assertThat(account.getCurrentBalance())
                    .withFailMessage("CurrentBalance should be equals")
                    .isEqualTo(accountDto.getCurrentBalance());
            softAssertions.assertThat(account.getOpenDate())
                    .withFailMessage("OpenDate should be equals")
                    .isEqualTo(accountDto.getOpenDate());
            softAssertions.assertThat(account.getCloseDate())
                    .withFailMessage("CloseDate should be equals")
                    .isEqualTo(accountDto.getCloseDate());
            softAssertions.assertThat(account.getIsActive())
                    .withFailMessage("IsActive should be equals")
                    .isEqualTo(accountDto.getIsActive());
            softAssertions.assertThat(account.getSalaryProject())
                    .withFailMessage("SalaryProject should be equals")
                    .isEqualTo(accountDto.getSalaryProject());
            softAssertions.assertThat(account.getCurrencyCode())
                    .withFailMessage("CurrencyCode should be equals")
                    .isEqualTo(accountDto.getCurrencyCode());
        });
    }
}