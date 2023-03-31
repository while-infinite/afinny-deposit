package by.afinny.deposit.unit.mapper;

import by.afinny.deposit.dto.AccountNumberDto;
import by.afinny.deposit.dto.CardDto;
import by.afinny.deposit.dto.ViewCardDto;
import by.afinny.deposit.dto.userservice.AccountDto;
import by.afinny.deposit.entity.Account;
import by.afinny.deposit.entity.Card;
import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.mapper.AccountMapper;
import by.afinny.deposit.mapper.AccountMapperImpl;
import by.afinny.deposit.mapper.CardMapper;
import org.assertj.core.api.SoftAssertions;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class AccountMapperTest {

    @InjectMocks
    private final AccountMapper accountMapper = new AccountMapperImpl();

    @Mock
    private CardMapper cardMapper;

    private static final UUID CLIENT_ID = UUID.randomUUID();
    private List<Account> accounts;
    private List<List<CardDto>> cardDtos;
    private Account account;

    @BeforeEach
    void setUp() {
        accounts = new ArrayList<>();
        cardDtos = new ArrayList<>();

        accounts.add(Account.builder()
                .cards(new ArrayList<>())
                .id(UUID.randomUUID())
                .accountNumber("accountNumber")
                .currencyCode(CurrencyCode.RUB)
                .clientId(CLIENT_ID)
                .closeDate(LocalDate.now())
                .openDate(LocalDate.now().minusYears(2))
                .currentBalance(BigDecimal.ONE).build());

        cardDtos.add(new ArrayList<>());

        account = Account.builder()
                .accountNumber("accountNumber")
                .build();
    }

    @Test
    @DisplayName("Check fields of Account and AccountDto are equals")
    void mapAccountToDtoList_thenReturn() {
        List<AccountDto> result = accountMapper.toAccountsDto(accounts);

        for (int i = 0; i < result.size(); i++) {
            verifyAccount(accounts.get(i), result.get(i));
        }
    }

    @Test
    @DisplayName("Check fields of Account and AccountWithCardDto are equals")
    void mapAccountToAccountWithCardDto_thenReturn() {
        for (int i = 0; i < accounts.size(); i++) {
            when(cardMapper.toCardsDto(accounts.get(i).getCards()))
                    .thenReturn(cardDtos.get(i));
        }

        List<CardDto> result = cardMapper.toCardsDto(accounts.get(0).getCards());

        for (int i = 0; i < result.size(); i++) {
            verifyCard(account.getCards().get(i), result.get(i));
            assertThat(result.get(i)).isEqualTo(cardDtos.get(i));
        }
    }

    void verifyAccount(Account account, AccountDto accountDto) {
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(account.getIsActive())
                    .withFailMessage("IsActive should be equals")
                    .isEqualTo(accountDto.getIsActive());
            softAssertions.assertThat(account.getCurrentBalance())
                    .withFailMessage("CurrentBalance should be equals")
                    .isEqualTo(accountDto.getCurrentBalance());
            softAssertions.assertThat(account.getCloseDate())
                    .withFailMessage("CloseDate should be equals")
                    .isEqualTo(accountDto.getCloseDate());
            softAssertions.assertThat(account.getOpenDate())
                    .withFailMessage("OpenDate should be equals")
                    .isEqualTo(accountDto.getOpenDate());
            softAssertions.assertThat(account.getSalaryProject())
                    .withFailMessage("SalaryProject should be equals")
                    .isEqualTo(accountDto.getSalaryProject());
            softAssertions.assertThat(account.getCurrencyCode())
                    .withFailMessage("CurrencyCode should be equals")
                    .isEqualTo(accountDto.getCurrencyCode());
            softAssertions.assertThat(account.getAccountNumber())
                    .withFailMessage("AccountNumber should be equals")
                    .isEqualTo(accountDto.getAccountNumber());
            softAssertions.assertThat(account.getClientId())
                    .withFailMessage("ClientId should be equals")
                    .isEqualTo(accountDto.getClientId());
        });
    }

    void verifyCard(Card card, CardDto cardDto) {
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(card.getId())
                .withFailMessage("AccountId should be equals")
                .isEqualTo(cardDto.getCardId());
        softAssertions.assertAll();
    }
}