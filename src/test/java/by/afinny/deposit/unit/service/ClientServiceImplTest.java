package by.afinny.deposit.unit.service;

import by.afinny.deposit.dto.ClientDto;
import by.afinny.deposit.dto.userservice.ClientByPhoneDto;
import by.afinny.deposit.entity.Account;
import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.exception.EntityNotFoundException;
import by.afinny.deposit.mapper.ClientMapper;
import by.afinny.deposit.mapper.ClientMapperImpl;
import by.afinny.deposit.openfeign.userservice.UserClient;
import by.afinny.deposit.repository.AccountRepository;
import by.afinny.deposit.service.impl.ClientServiceImpl;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ActiveProfiles("test")
public class ClientServiceImplTest {

    @InjectMocks
    private ClientServiceImpl clientService;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private UserClient userClient;
    @Spy
    private ClientMapper clientMapper = new ClientMapperImpl();

    private final UUID CLIENT_ID = UUID.randomUUID();
    private final String FIRST_NAME = "Ivan";
    private final String LAST_NAME = "Ivanov";
    private final String MIDDLE_NAME = "Ivanovich";
    private final String MOBILE_PHONE = "+79999999999";
    private final CurrencyCode CURRENCY_CODE = CurrencyCode.RUB;

    private ClientByPhoneDto clientByPhoneDto;
    private ClientDto clientDto;
    private Account account;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(UUID.randomUUID())
                .accountNumber("accountNumber")
                .clientId(CLIENT_ID)
                .currencyCode(CURRENCY_CODE)
                .closeDate(LocalDate.now())
                .openDate(LocalDate.now().minusYears(2))
                .currentBalance(BigDecimal.ONE).build();

        clientByPhoneDto = ClientByPhoneDto.builder()
                .clientId(CLIENT_ID)
                .firstName(FIRST_NAME)
                .middleName(MIDDLE_NAME)
                .lastName(LAST_NAME).build();
    }

    @Test
    @DisplayName("If client was found by mobile phone and currency code, then return ClientDto")
    void getClientByPhoneNumber_shouldReturnClientDtoByCurrencyCodeAndMobilePhone() {
        //Arrange
        Mockito.when(userClient.getClientByPhone(MOBILE_PHONE)).thenReturn(ResponseEntity.ok(clientByPhoneDto));
        Mockito.when(accountRepository.getAccountsByClientIdAndCurrencyCode(clientByPhoneDto.getClientId(), CURRENCY_CODE))
                .thenReturn(Optional.of(account));
        //ACT
        clientDto = clientService.getClientByPhoneNumber(CLIENT_ID, CURRENCY_CODE, MOBILE_PHONE);
        //VERIFY
        verifyClientDto(clientDto, account, clientByPhoneDto);
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

    @Test
    @DisplayName("If account wasn't found by currency code, then throw EntityNotFoundException")
    void getClientByPhoneNumber_ifCurrencyCodeNotExist_thenThrowEntityNotFoundException() {
        //Arrange
        Mockito.when(userClient.getClientByPhone(MOBILE_PHONE)).thenReturn(ResponseEntity.ok(clientByPhoneDto));
        Mockito.when(accountRepository.getAccountsByClientIdAndCurrencyCode(clientByPhoneDto.getClientId(), CURRENCY_CODE))
                .thenThrow(new EntityNotFoundException("no currencyCode " + CURRENCY_CODE + " by this was found"));
        //ACT
        ThrowableAssert.ThrowingCallable getClientDto = () -> clientService.getClientByPhoneNumber(CLIENT_ID, CURRENCY_CODE, MOBILE_PHONE);
        //VERIFY
        Assertions.assertThatThrownBy(getClientDto).isNotNull();
    }

    private void verifyClientDto(ClientDto clientDto, Account account, ClientByPhoneDto clientByPhoneDto) {
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(clientDto.getClientId()).isEqualTo(clientByPhoneDto.getClientId());
            softAssertions.assertThat(clientDto.getFirstName()).isEqualTo(clientByPhoneDto.getFirstName());
            softAssertions.assertThat(clientDto.getMiddleName()).isEqualTo(clientByPhoneDto.getMiddleName());
            softAssertions.assertThat(clientDto.getLastName()).isEqualTo(clientByPhoneDto.getLastName());
            softAssertions.assertThat(clientDto.getAccountNumber()).isEqualTo(account.getAccountNumber());
        });
    }
}
