package by.afinny.deposit.integration.controller;

import by.afinny.deposit.integration.config.annotation.TestWithPostgresContainer;
import by.afinny.deposit.dto.ClientDto;
import by.afinny.deposit.dto.userservice.ClientByPhoneDto;
import by.afinny.deposit.entity.Account;
import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.exception.EntityNotFoundException;
import by.afinny.deposit.mapper.ClientMapper;
import by.afinny.deposit.mapper.ClientMapperImpl;
import by.afinny.deposit.openfeign.userservice.UserClient;
import by.afinny.deposit.repository.AccountRepository;
import by.afinny.deposit.utils.MappingUtils;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SuppressWarnings("FieldCanBeLocal")
@TestWithPostgresContainer
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("ClientByPhoneControllerIT")
public class ClientByPhoneControllerTestIT {

    private final UUID CLIENT_ID = UUID.randomUUID();
    private final String FIRST_NAME = "Ivan";
    private final String LAST_NAME = "Ivanov";
    private final String MIDDLE_NAME = "Ivanovich";
    private final String ACCOUNT_NUMBER = "accountNumber";
    private final String MOBILE_PHONE = "+79999999999";
    private final CurrencyCode CURRENCY_CODE = CurrencyCode.RUB;
    private Account account;
    private ClientDto clientDto;
    private ClientByPhoneDto clientByPhoneDto;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MappingUtils mappingUtils;
    @MockBean
    private UserClient userClient;
    @Spy
    private final ClientMapper clientMapper = new ClientMapperImpl();

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
    @DisplayName("If client point correct phone and currency code, then return ok status and client dto")
    void getClientByPhone_ifClientFound_thenReturnOkAndReturnClientDto() throws Exception {
        //ARRANGE
        Mockito.when(userClient.getClientByPhone(MOBILE_PHONE)).thenReturn(ResponseEntity.ok(clientByPhoneDto));
        //ACT
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/auth/accounts")
                        .param("clientId", CLIENT_ID.toString())
                        .param("mobilePhone", MOBILE_PHONE)
                        .param("currency_code", CURRENCY_CODE.name()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        //VERIFY
        Account account = accountRepository.getAccountsByClientIdAndCurrencyCode(clientByPhoneDto.getClientId(), CURRENCY_CODE)
                .orElseThrow();
        clientDto = clientMapper.toClientDto(clientByPhoneDto, account);
        verifyBody(mappingUtils.asJsonString(clientDto), result.getResponse().getContentAsString());
        verifyClientDto(clientDto, clientByPhoneDto, account);
    }

    @Test
    @DisplayName("If client point incorrect phone, then throw entity not found exception")
    void getClientByPhone_ifClientFillIncorrectPhoneOrCurrencyCode_thenReturnBadRequest() throws Exception {
        //Arrange
        Mockito.when(userClient.getClientByPhone(MOBILE_PHONE)).thenThrow(EntityNotFoundException.class);
        //ACT & VERIFY
        mockMvc.perform(MockMvcRequestBuilders.get("/auth/accounts")
                        .param("clientId", CLIENT_ID.toString())
                        .param("mobilePhone", MOBILE_PHONE)
                        .param("currency_code", CURRENCY_CODE.name()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    private void verifyClientDto(ClientDto clientDto, ClientByPhoneDto clientByPhoneDto, Account accountFromDb) {
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(clientDto.getClientId()).isEqualTo(clientByPhoneDto.getClientId());
            softAssertions.assertThat(clientDto.getFirstName()).isEqualTo(clientByPhoneDto.getFirstName());
            softAssertions.assertThat(clientDto.getMiddleName()).isEqualTo(clientByPhoneDto.getMiddleName());
            softAssertions.assertThat(clientDto.getLastName()).isEqualTo(clientByPhoneDto.getLastName());
            softAssertions.assertThat(clientDto.getAccountNumber()).isEqualTo(accountFromDb.getAccountNumber());
        });
    }

    private void verifyBody(String expectedBody, String actualBody) {
        assertThat(actualBody).isEqualTo(expectedBody);
    }
}
