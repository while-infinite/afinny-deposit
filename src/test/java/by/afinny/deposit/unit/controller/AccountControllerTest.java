package by.afinny.deposit.unit.controller;

import by.afinny.deposit.controller.userservice.AccountController;
import by.afinny.deposit.dto.userservice.AccountDto;
import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.service.impl.AccountServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountServiceImpl accountService;

    private List<AccountDto> accountDtoList;
    private final UUID CLIENT_ID = UUID.randomUUID();

    @BeforeAll
    public void setUp() {
        accountDtoList = new ArrayList<>();
        accountDtoList.add(AccountDto.builder()
                .accountNumber("1")
                .clientId(CLIENT_ID)
                .currentBalance(BigDecimal.ONE)
                .isActive(true)
                .salaryProject("salaryProject")
                .currencyCode(CurrencyCode.RUB).build());
    }

    @Test
    @DisplayName("if the list of active accounts was successfully received then return status OK")
    void getClientCurrentAccounts_ifSuccess_thenStatus200() throws Exception {
        //ARRANGE
        when(accountService.getActiveAccounts(CLIENT_ID)).thenReturn(accountDtoList);

        //ACT & VERIFY
        MvcResult result = mockMvc.perform(
                get("/accounts")
                        .param("clientId", CLIENT_ID.toString()))
                .andExpect(status().isOk())
                .andReturn();
        verifyBody(asJsonString(accountDtoList), result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("if the list of current accounts wasn't successfully received then return status INTERNAL SERVER ERROR")
    void getClientCurrentAccounts_ifNotSuccess_thenStatus500() throws Exception {
        //ARRANGE
        when(accountService.getActiveAccounts(CLIENT_ID)).thenThrow(new RuntimeException());

        //ACT & VERIFY
        MvcResult result = mockMvc.perform(
                get("/accounts")
                        .param("clientId", CLIENT_ID.toString()))
                .andExpect(status().isInternalServerError())
                .andReturn();
    }

    private static String asJsonString(final Object obj) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(obj);
    }

    private void verifyBody(String expectedBody, String actualBody) {
        assertEquals(actualBody, expectedBody);
    }
}