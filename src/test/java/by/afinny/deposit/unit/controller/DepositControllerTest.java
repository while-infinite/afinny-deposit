package by.afinny.deposit.unit.controller;

import by.afinny.deposit.controller.DepositController;
import by.afinny.deposit.dto.ActiveDepositDto;
import by.afinny.deposit.dto.AutoRenewalDto;
import by.afinny.deposit.dto.DepositDto;
import by.afinny.deposit.dto.RefillDebitCardDto;
import by.afinny.deposit.dto.WithdrawDepositDto;
import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.entity.constant.SchemaName;
import by.afinny.deposit.exception.handler.ExceptionHandlerController;
import by.afinny.deposit.service.AgreementService;
import by.afinny.deposit.service.DepositService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import by.afinny.deposit.exception.EntityNotFoundException;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@WebMvcTest(DepositController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class DepositControllerTest {

    @MockBean
    private DepositService depositService;
    @MockBean
    private AgreementService agreementService;

    private MockMvc mockMvc;

    private final UUID AGREEMENT_ID = UUID.randomUUID();
    private final UUID CLIENT_ID = UUID.randomUUID();
    private final UUID CARD_ID = UUID.randomUUID();

    private DepositDto depositDto;
    private AutoRenewalDto autoRenewalDto;
    private WithdrawDepositDto withdrawDepositDto;
    private List<ActiveDepositDto> activeDepositDtoList;
    private RefillDebitCardDto refillDebitCardDto;

    @BeforeAll
    void setUp() {
        mockMvc = standaloneSetup(new DepositController(depositService, agreementService))
                .setControllerAdvice(new ExceptionHandlerController()).build();

        withdrawDepositDto = WithdrawDepositDto.builder()
                .cardNumber("1234567890123456")
                .build();

        depositDto = DepositDto.builder()
                .cardNumber("1234567890123456")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusMonths(12L))
                .interestRate(new BigDecimal(10))
                .currentBalance(new BigDecimal(10))
                .autoRenewal(true)
                .name("name")
                .currencyCode(CurrencyCode.RUB)
                .schemaName(SchemaName.FIXED)
                .isCapitalization(true)
                .isRevocable(true).build();

        activeDepositDtoList = List.of(
                ActiveDepositDto.builder()
                        .agreementId(UUID.randomUUID())
                        .startDate(LocalDateTime.now())
                        .endDate(LocalDateTime.now().plusDays(1))
                        .currentBalance(new BigDecimal("1000000.00"))
                        .productName("product name")
                        .currencyCode(CurrencyCode.RUB)
                        .cardNumber("1111222233334444").build());

        autoRenewalDto = AutoRenewalDto.builder()
                .autoRenewal(true)
                .build();

        activeDepositDtoList = List.of(
                ActiveDepositDto.builder()
                        .agreementId(UUID.randomUUID())
                        .startDate(LocalDateTime.now())
                        .endDate(LocalDateTime.now().plusDays(1))
                        .currentBalance(new BigDecimal("1000000.00"))
                        .productName("product name")
                        .currencyCode(CurrencyCode.RUB)
                        .cardNumber("1111222233334444").build());

        refillDebitCardDto = RefillDebitCardDto.builder()
                .sum("1000")
                .accountNumber("123")
                .build();
    }

    @Test
    @DisplayName("if agreement with incoming id was successfully found then return status OK")
    void getAgreement_ShouldBeReturn200_OK() throws Exception {
        //ACT
        MvcResult result = mockMvc.perform(
                        patch("/auth/deposits/{agreementId}/revocation", AGREEMENT_ID)
                                .param("clientId", CLIENT_ID.toString())
                                .characterEncoding(Charset.defaultCharset())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(withdrawDepositDto)))
                .andExpect(status().isOk())
                .andReturn();

        //VERIFY
        verifyWithdrawDepositDtoRequestParameter(result);
    }

    @Test
    @DisplayName("if agreement with incoming id wasn't successfully found then return status INTERNAL_SERVER_ERROR")
    void getAgreement_ifAgreementNotFound_then500_INTERNAL_SERVER_ERROR() throws Exception {
        //ARRANGE
        doThrow(RuntimeException.class).when(agreementService).earlyWithdrawalDeposit(any(UUID.class), any(UUID.class), any(WithdrawDepositDto.class));

        //ACT
        ResultActions result = mockMvc.perform(
                patch("/auth/deposits/{agreementId}/revocation", AGREEMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(withdrawDepositDto)));

        //VERIFY
        result.andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("If successful then return depositDto")
    void getDeposit_shouldReturnDepositDto() throws Exception {
        //ARRANGE
        when(depositService.getDeposit(CLIENT_ID, AGREEMENT_ID, CARD_ID)).thenReturn(depositDto);

        //ACT
        MvcResult result = mockMvc.perform(
                        get("/auth/deposits/{agreementId}", AGREEMENT_ID.toString())
                                .param("cardId", CARD_ID.toString())
                                .param("clientId", CLIENT_ID.toString()))
                .andExpect(status().isOk())
                .andReturn();
        //VERIFY
        verifyBody(asJsonString(depositDto), result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("If unsuccessful then return INTERNAL_SERVER_ERROR")
    void getDeposit_ifNotSuccess_then500_INTERNAL_SERVER_ERROR() throws Exception {
        //ARRANGE
        when(depositService.getDeposit(any(), any(), any())).thenThrow(EntityNotFoundException.class);

        //ACT&VERIFY
        mockMvc.perform(
                        get("/auth/deposits/{agreementId}", AGREEMENT_ID.toString())
                                .param("cardId", CARD_ID.toString())
                                .param("clientId", CLIENT_ID.toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("If deposits was successfully found then return list of deposits")
    void getActiveDeposits_shouldReturnListDepositDto() throws Exception {
        //ARRANGE
        when(depositService.getActiveDeposits(any(UUID.class))).thenReturn(activeDepositDtoList);
        //ACT & VERIFY
        MvcResult result = mockMvc.perform(get("/auth/deposits")
                        .param("clientId", CLIENT_ID.toString()))
                .andExpect(status().isOk())
                .andReturn();
        verifyBody(result.getResponse().getContentAsString(), asJsonString(activeDepositDtoList));
    }

    @Test
    @DisplayName("If the list of deposits wasn't successfully received then return status INTERNAL SERVER ERROR")
    void getActiveDeposits_ifNotSuccess_thenStatus500() throws Exception {
        //ARRANGE
        when(depositService.getActiveDeposits(any(UUID.class))).thenThrow(new RuntimeException());
        //ACT & VERIFY
        mockMvc.perform(get("/auth/deposits")
                        .param("clientId", CLIENT_ID.toString()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("If successfully update auto renewal then return status OK")
    void getActiveProducts_shouldNotReturnContent() throws Exception {
        //ACT
        ResultActions resultActions = mockMvc.perform(patch(DepositController.URL_DEPOSITS + DepositController.URL_DEPOSITS_AUTO_RENEWAL, AGREEMENT_ID)
                .param("clientId", CLIENT_ID.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(autoRenewalDto)));

        //VERIFY
        resultActions.andExpect(status().isOk());

        String creditIdParam = Arrays.stream(resultActions.andReturn().getRequest().getRequestURI().split("/"))
                .skip(3).findFirst().orElse(null);
        assertThat(creditIdParam)
                .isNotNull()
                .isEqualTo(AGREEMENT_ID.toString());
    }

    @Test
    @DisplayName("If update auto renewal failed then return InternalServerError")
    void getActiveProducts_ifUpdateFailed_thenReturnInternalServerError() throws Exception {
        //ARRANGE
        doThrow(EntityNotFoundException.class).when(agreementService).updateAutoRenewal(any(UUID.class), any(UUID.class), any(AutoRenewalDto.class));
        //ACT
        ResultActions perform = mockMvc.perform(patch(DepositController.URL_DEPOSITS + DepositController.URL_DEPOSITS_AUTO_RENEWAL, AGREEMENT_ID)
                .param("clientId", CLIENT_ID.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(autoRenewalDto)));
        //VERIFY
        perform.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("If successfully refill deposit then return OK ")
    void refillUserDebitCard_shouldBeReturn200_OK() throws Exception {
        //ACT
        MvcResult result = mockMvc.perform(
                post("/auth/deposits/refill")
                        .param("clientId", CLIENT_ID.toString())
                        .characterEncoding(Charset.defaultCharset())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(refillDebitCardDto)))
                .andExpect(status().isOk())
                .andReturn();

        //VERIFY
        assertThat(result.getRequest().getContentAsString()).isEqualTo(asJsonString(refillDebitCardDto));
    }

    @Test
    @DisplayName("If refill failed then return InternalServerError")
    void refillUserDebitCard_ifRefillFailed_then500_INTERNAL_SERVER_ERROR() throws Exception {
        //ARRANGED
        doThrow(EntityNotFoundException.class).when(depositService).refillUserDebitCard(any(UUID.class), any(RefillDebitCardDto.class));

        //ACT&VERIFY
        mockMvc.perform(
                        post("/auth/deposits/refill")
                                .param("clientId", CLIENT_ID.toString())
                                .characterEncoding(Charset.defaultCharset())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(refillDebitCardDto)))
                .andExpect(status().isBadRequest());

    }

    private static String asJsonString(final Object obj) throws JsonProcessingException {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .writeValueAsString(obj);
    }

    private void verifyBody(String expectedBody, String actualBody) {
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    private void verifyWithdrawDepositDtoRequestParameter(MvcResult result)
            throws UnsupportedEncodingException, JsonProcessingException {
        assertThat(result.getRequest().getContentAsString()).isEqualTo(asJsonString(withdrawDepositDto));
    }
}