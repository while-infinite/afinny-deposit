package by.afinny.deposit.integration.controller;

import by.afinny.deposit.dto.userservice.AccountDto;
import by.afinny.deposit.entity.Account;
import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.integration.config.annotation.TestWithPostgresContainer;
import by.afinny.deposit.mapper.AccountMapper;
import by.afinny.deposit.repository.AccountRepository;
import by.afinny.deposit.utils.MappingUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestWithPostgresContainer
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("AccountControllerIT")
public class AccountControllerIT {

    private final UUID CLIENT_ID = UUID.randomUUID();
    private Account account;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MappingUtils mappingUtils;
    @Autowired
    private AccountMapper accountMapper;

    @BeforeAll
    void setUp() {
        account = Account
                .builder()
                .accountNumber("1")
                .clientId(CLIENT_ID)
                .currentBalance(new BigDecimal(100))
                .openDate(LocalDate.of(2020, 1, 15))
                .closeDate(LocalDate.of(2030, 1, 15))
                .isActive(true)
                .salaryProject("salaryProject")
                .currencyCode(CurrencyCode.RUB)
                .blockedSum(new BigDecimal(10))
                .build();
    }

    @Test
    @DisplayName("Should return active accounts list from test container")
    void getAccounts_shouldReturnAccounts() throws Exception {
        //ARRANGE
        account.setIsActive(Boolean.TRUE);
        accountRepository.save(account);
        //ACT
        MvcResult result = mockMvc.perform(get("/accounts")
                .param("clientId", CLIENT_ID.toString()))
                .andExpect(status().isOk())
                .andReturn();
        List<AccountDto> accountDtoResult = mappingUtils.getObjectListFromJson(result.getResponse().getContentAsString(), AccountDto.class);
        List<AccountDto> accountDtoDB = accountMapper.toAccountsDto(accountRepository.findAll());
        //VERIFY
        verifyAccounts(accountDtoResult, accountDtoDB);
    }

    @Test
    @DisplayName("Should return empty list in case no active accounts")
    void getAccounts_shouldReturnEmptyAccountsList() throws Exception {
        //ARRANGE
        account.setIsActive(Boolean.FALSE);
        accountRepository.save(account);
        //ACT
        MvcResult result = mockMvc.perform(get("/accounts")
                .param("clientId", CLIENT_ID.toString()))
                .andExpect(status().isOk())
                .andReturn();
        List<AccountDto> accountDtoResult = mappingUtils.getObjectListFromJson(result.getResponse().getContentAsString(), AccountDto.class);
        //VERIFY
        assertThat(accountDtoResult.isEmpty())
                .isTrue();
    }

    private void verifyAccounts(List<AccountDto> result, List<AccountDto> actual) {
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(actual);
    }
}
