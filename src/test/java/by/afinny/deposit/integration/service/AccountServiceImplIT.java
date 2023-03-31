package by.afinny.deposit.integration.service;

import by.afinny.deposit.dto.AccountWithCardInfoDto;
import by.afinny.deposit.dto.ViewCardDto;
import by.afinny.deposit.dto.userservice.AccountDto;
import by.afinny.deposit.entity.Account;
import by.afinny.deposit.entity.Card;
import by.afinny.deposit.entity.CardProduct;
import by.afinny.deposit.entity.constant.*;
import by.afinny.deposit.exception.EntityNotFoundException;
import by.afinny.deposit.integration.config.annotation.TestWithPostgresContainer;
import by.afinny.deposit.mapper.AccountMapper;
import by.afinny.deposit.mapper.CardMapper;
import by.afinny.deposit.repository.AccountRepository;
import by.afinny.deposit.repository.CardProductRepository;
import by.afinny.deposit.repository.CardRepository;
import by.afinny.deposit.service.AccountService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.jdbc.Sql;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static by.afinny.deposit.entity.constant.CardStatus.BLOCKED;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

@SuppressWarnings("FieldCanBeLocal")
@TestWithPostgresContainer
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("AccountServiceIT")
public class AccountServiceImplIT {

    private final UUID CLIENT_ID = UUID.randomUUID();
    private final String CARD_NUMBER = "123-122-123-124 ";
    private Account account;
    private Card card;
    private CardProduct cardProduct;
    @SpyBean
    private AccountService accountService;
    @SpyBean
    private AccountRepository accountRepository;
    @SpyBean
    private CardProductRepository cardProductRepository;
    @SpyBean
    private CardRepository cardRepository;
    @SpyBean
    private AccountMapper accountMapper;
    @SpyBean
    private CardMapper cardMapper;

    @BeforeEach
    void setUp() {
        account = Account
                .builder()
                .accountNumber("1")
                .clientId(CLIENT_ID)
                .currentBalance(new BigDecimal("100.1234"))
                .openDate(LocalDate.of(2020, 1, 15))
                .closeDate(LocalDate.of(2030, 1, 15))
                .isActive(true)
                .salaryProject("salaryProject")
                .currencyCode(CurrencyCode.RUB)
                .blockedSum(new BigDecimal("10.1234"))
                .build();

        card = Card.builder()
                .cardNumber(CARD_NUMBER)
                .transactionLimit(new BigDecimal("100.1234"))
                .expirationDate(LocalDate.now().plusDays(1))
                .holderName("Peter Parker")
                .status(CardStatus.ACTIVE)
                .digitalWallet(DigitalWallet.APPLEPAY)
                .isDefault(true)
                .build();

        cardProduct = CardProduct
                .builder()
                .cardName("TEST")
                .paymentSystem(PaymentSystem.VISA)
                .coBrand(CoBrand.AEROFLOT)
                .isVirtual(Boolean.FALSE)
                .premiumStatus(PremiumStatus.CLASSIC)
                .servicePrice(BigDecimal.valueOf(0))
                .productPrice(BigDecimal.valueOf(0))
                .currencyCode(CurrencyCode.RUB)
                .isActive(Boolean.TRUE)
                .cardDuration(5)
                .build();
    }

    @Test
    @DisplayName("Should return active accounts list from test container")
    void getActiveAccounts_shouldReturnAccounts() {
        //ARRANGE
        account.setIsActive(Boolean.TRUE);
        account = accountRepository.save(account);
        //ACT
        List<AccountDto> accountDtoResult = accountService.getActiveAccounts(CLIENT_ID);
        List<AccountDto> accountDtoActual = accountMapper.toAccountsDto(accountRepository.findByClientIdAndIsActiveTrue(CLIENT_ID));
        //VERIFY
        verifyAccounts(accountDtoResult, accountDtoActual);
    }

    @Test
    @DisplayName("Should return empty accounts list in case no active accounts")
    void getActiveAccounts_shouldReturnEmptyAccounts() {
        //ARRANGE
        account.setIsActive(Boolean.FALSE);
        account = accountRepository.save(account);
        //ACT
        List<AccountDto> accountDtoResult = accountService.getActiveAccounts(CLIENT_ID);
        //VERIFY
        assertThat(accountDtoResult.isEmpty())
                .isTrue();
    }

    @Test
    @DisplayName("Should return cards list of active accounts from test container")
    void getActiveCards_shouldReturnCardsOfActiveAccounts() {
        //ARRANGE
        account.setIsActive(Boolean.TRUE);
        account = accountRepository.save(account);
        cardProduct = cardProductRepository.save(cardProduct);
        card.setAccount(account);
        card.setCardProduct(cardProduct);
        card = cardRepository.save(card);
        //ACT
        List<AccountWithCardInfoDto> accountCardsResult = accountService.getActiveAccountsWithCard(CLIENT_ID);
        List<AccountWithCardInfoDto> accountCardsActual = getActiveCardsFromAccounts(CLIENT_ID);
        //VERIFY
        verifyActiveCards(accountCardsResult, accountCardsActual);
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException if no active accounts in db")
    void getActiveCards_shouldReturnEmptyCards() {
        //ARRANGE
        account.setIsActive(Boolean.FALSE);
        account = accountRepository.save(account);
        cardProduct = cardProductRepository.save(cardProduct);
        card.setAccount(account);
        card.setCardProduct(cardProduct);
        card = cardRepository.save(card);
        //ACT
        Throwable thrown = catchThrowable(() -> {
            accountService.getActiveAccountsWithCard(CLIENT_ID);
        });
        //VERIFY
        assertThat(thrown)
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("There are no active accounts by id");
    }

    @Test
    @DisplayName("Should return empty cards list of active accounts if all cards in blocked state")
    void getBlockedCards_shouldReturnEmptyCards() {
        //ARRANGE
        account.setIsActive(Boolean.TRUE);
        account = accountRepository.save(account);
        cardProduct = cardProductRepository.save(cardProduct);
        card.setAccount(account);
        card.setCardProduct(cardProduct);
        card.setStatus(CardStatus.BLOCKED);
        card = cardRepository.save(card);
        //ACT
        List<AccountWithCardInfoDto> accountCardsResult = accountService.getActiveAccountsWithCard(CLIENT_ID);
        //VERIFY
        assertThat(accountCardsResult.isEmpty())
                .isTrue();
    }

    @Test
    @DisplayName("Should return card from test container by account id and card id")
    void getCardByCardId_shouldReturnViewCard() {
        //ARRANGE
        account.setIsActive(Boolean.TRUE);
        account = accountRepository.save(account);
        cardProduct = cardProductRepository.save(cardProduct);
        card.setAccount(account);
        card.setCardProduct(cardProduct);
        card.setStatus(CardStatus.BLOCKED);
        card = cardRepository.save(card);
        //ACT
        ViewCardDto viewCardDtoResult = accountService.getViewCardByCardId(CLIENT_ID, card.getId());
        ViewCardDto viewCardDtoActual = cardMapper.toViewCardDto(cardRepository
                .findByAccountClientIdAndId(CLIENT_ID, card.getId())
                .orElseThrow());
        //VERIFY
        verifyViewCardDto(viewCardDtoResult, viewCardDtoActual);
    }

    @ParameterizedTest
    @MethodSource("generateData")
    @DisplayName("If account id or card id is incorrect then throw EntityNotFoundException")
    void getCardByCardId_ifNotSuccess_thenThrow_EntityNotFoundException(UUID clientID, UUID cardID) {
        //ARRANGE
        account.setIsActive(Boolean.TRUE);
        account = accountRepository.save(account);
        cardProduct = cardProductRepository.save(cardProduct);
        card.setAccount(account);
        card.setCardProduct(cardProduct);
        card.setStatus(CardStatus.BLOCKED);
        card = cardRepository.save(card);
        //ACT
        Throwable thrown = catchThrowable(() -> {
            accountService.getViewCardByCardId(clientID, cardID);
        });
        //VERIFY
        assertThat(thrown)
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("card with card id");
    }

    private Stream<Arguments> generateData() {
        return Stream.of(
                Arguments.of(CLIENT_ID, UUID.randomUUID()),
                Arguments.of(UUID.randomUUID(), UUID.randomUUID()));
    }

    private List<AccountWithCardInfoDto> getActiveCardsFromAccounts(List<Account> accounts) {
        List<Card> cards = accounts
                .stream()
                .flatMap(a -> a.getCards().stream())
                .filter(c -> c.getStatus() != BLOCKED)
                .collect(Collectors.toList());
        return accountMapper.toAccountsWithCardsDto(cards);
    }

    private List<AccountWithCardInfoDto> getActiveCardsFromAccounts(UUID clientId) {
        List<Account> accounts = accountRepository.findByClientIdAndIsActiveTrue(clientId);
        if(accounts.isEmpty()) {
            throw new EntityNotFoundException("There are no active accounts by id " + clientId);
        }
        return getActiveCardsFromAccounts(accounts);
    }

    private void verifyActiveCards(List<AccountWithCardInfoDto> result, List<AccountWithCardInfoDto> actual) {
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(actual);
    }


    private void verifyAccounts(List<AccountDto> result, List<AccountDto> actual) {
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(actual);
    }

    private void verifyViewCardDto(ViewCardDto result, ViewCardDto actual) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(result.getCardId())
                    .isEqualTo(actual.getCardId());
            softAssertions.assertThat(result.getHolderName())
                    .isEqualTo(actual.getHolderName());
            softAssertions.assertThat(result.getStatus())
                    .isEqualTo(actual.getStatus());
            softAssertions.assertThat(result.getCardNumber())
                    .isEqualTo(actual.getCardNumber());
            softAssertions.assertThat(result.getExpirationDate())
                    .isEqualTo(actual.getExpirationDate());
            softAssertions.assertThat(result.getCardName())
                    .isEqualTo(actual.getCardName());
            softAssertions.assertThat(result.getPaymentSystem())
                    .isEqualTo(actual.getPaymentSystem());
            softAssertions.assertThat(result.getCurrencyCode())
                    .isEqualTo(actual.getCurrencyCode());
            softAssertions.assertThat(result.getCardBalance())
                    .isEqualTo(actual.getCardBalance());
            softAssertions.assertThat(result.getAccountId())
                    .isEqualTo(actual.getAccountId());
            softAssertions.assertThat(result.getAccountNumber())
                    .isEqualTo(actual.getAccountNumber());
        });
    }
}
