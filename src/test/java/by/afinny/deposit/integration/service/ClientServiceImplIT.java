package by.afinny.deposit.integration.service;

import by.afinny.deposit.integration.config.annotation.TestWithPostgresContainer;
import by.afinny.deposit.dto.ClientDto;
import by.afinny.deposit.dto.userservice.ClientByPhoneDto;
import by.afinny.deposit.entity.Account;
import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.exception.EntityNotFoundException;
import by.afinny.deposit.mapper.ClientMapper;
import by.afinny.deposit.openfeign.userservice.UserClient;
import by.afinny.deposit.repository.AccountRepository;
import by.afinny.deposit.service.ClientService;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@SuppressWarnings("FieldCanBeLocal")
@TestWithPostgresContainer
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@EnableConfigurationProperties
@DisplayName("ClientServiceIT")
public class ClientServiceImplIT {

    private final UUID CLIENT_ID = UUID.randomUUID();
    private final String FIRST_NAME = "Ivan";
    private final String LAST_NAME = "Ivanov";
    private final String MIDDLE_NAME = "Ivanovich";
    private final String ACCOUNT_NUMBER = "accountNumber";
    private final String MOBILE_PHONE = "+79999999999";
    private final CurrencyCode CURRENCY_CODE = CurrencyCode.RUB;
    private ClientByPhoneDto clientByPhoneDto;
    private ClientDto clientDto;
    private Account account;
    @SpyBean
    private ClientService clientService;
    @SpyBean
    private AccountRepository accountRepository;
    @SpyBean
    private ClientMapper clientMapper;
    @MockBean
    private UserClient userClient;

    @BeforeAll
    void setUp() {
        account = Account.builder()
                .id(UUID.randomUUID())
                .accountNumber(ACCOUNT_NUMBER)
                .clientId(CLIENT_ID)
                .currencyCode(CURRENCY_CODE)
                .closeDate(LocalDate.now())
                .openDate(LocalDate.now().minusYears(2))
                .isActive(true)
                .salaryProject("ff")
                .blockedSum(BigDecimal.ZERO)
                .currentBalance(BigDecimal.ONE).build();

        clientByPhoneDto = ClientByPhoneDto.builder()
                .clientId(CLIENT_ID)
                .firstName(FIRST_NAME)
                .middleName(MIDDLE_NAME)
                .lastName(LAST_NAME).build();
    }

    @BeforeEach
    void save() {
        accountRepository.save(account);
    }

    @Test
    @DisplayName("If client was found by mobile phone and currency code, then return client dto")
    void getClientByPhoneNumber_shouldReturnClientDtoByCurrencyCodeAndMobilePhone() {
        //ARRANGE
        Mockito.when(userClient.getClientByPhone(MOBILE_PHONE)).thenReturn(ResponseEntity.ok(clientByPhoneDto));
        //ACT
        clientDto = clientService.getClientByPhoneNumber(CLIENT_ID, CURRENCY_CODE, MOBILE_PHONE);
        Account account = accountRepository.getAccountsByClientIdAndCurrencyCode(clientByPhoneDto.getClientId(), CURRENCY_CODE)
                .orElseThrow();
        clientDto = clientMapper.toClientDto(clientByPhoneDto, account);
        //VERIFY
        verifyClientDto(clientDto, clientByPhoneDto, account);
    }

    @Test
    @DisplayName("If client wasn't found by mobile phone, then throw EntityNotFoundException")
    void getClientByPhoneNumber_ifMobilePhoneNotExist_thenThrowEntityNotFoundException() {
        //Arrange
        Mockito.when(userClient.getClientByPhone(MOBILE_PHONE)).
                thenThrow(new EntityNotFoundException("no client with the mobile phone " + MOBILE_PHONE + " was found"));
        //ACT
        ThrowableAssert.ThrowingCallable getClientDto = () -> clientService.getClientByPhoneNumber(CLIENT_ID, CURRENCY_CODE, MOBILE_PHONE);
        //VERIFY
        Assertions.assertThatThrownBy(getClientDto).isNotNull();
    }

    private void verifyClientDto(ClientDto clientDto, ClientByPhoneDto clientByPhoneDto, Account accountFromDb) {
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(clientDto.getClientId())
                    .isEqualTo(clientByPhoneDto.getClientId());
            softAssertions.assertThat(clientDto.getFirstName())
                    .isEqualTo(clientByPhoneDto.getFirstName());
            softAssertions.assertThat(clientDto.getMiddleName())
                    .isEqualTo(clientByPhoneDto.getMiddleName());
            softAssertions.assertThat(clientDto.getLastName())
                    .isEqualTo(clientByPhoneDto.getLastName());
            softAssertions.assertThat(clientDto.getAccountNumber())
                    .isEqualTo(accountFromDb.getAccountNumber());
        });
    }
}
