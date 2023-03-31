package by.afinny.deposit.unit.controller;

import by.afinny.deposit.controller.CardController;
import by.afinny.deposit.dto.AccountWithCardInfoDto;
import by.afinny.deposit.dto.CardDebitLimitDto;
import by.afinny.deposit.dto.CardDto;
import by.afinny.deposit.dto.CardInfoDto;
import by.afinny.deposit.dto.CardNumberDto;
import by.afinny.deposit.dto.CardStatusDto;
import by.afinny.deposit.dto.CreatePaymentDepositDto;
import by.afinny.deposit.dto.NewPinCodeDebitCardDto;
import by.afinny.deposit.dto.ViewCardDto;
import by.afinny.deposit.entity.constant.CardStatus;
import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.entity.constant.DigitalWallet;
import by.afinny.deposit.entity.constant.PaymentSystem;
import by.afinny.deposit.exception.CardStatusesAreEqualsException;
import by.afinny.deposit.exception.EntityNotFoundException;
import by.afinny.deposit.service.impl.AccountServiceImpl;
import by.afinny.deposit.service.impl.CardServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountServiceImpl accountService;
    @MockBean
    private CardServiceImpl cardService;

    public static final String URL_WRITE_OFF_SUM = "/auth/deposit-cards/";
    private static final String FORMAT_DATE = "yyyy-MM-dd";
    private static final UUID CLIENT_ID = UUID.randomUUID();
    private static final UUID CARD_ID = UUID.randomUUID();
    private List<AccountWithCardInfoDto> accountWithCardInfoDtoList;
    private CardStatusDto cardStatusDto;
    private ViewCardDto viewCardDto;
    private CardDebitLimitDto cardDebitLimitDto;
    private CardNumberDto cardNumberDto;
    private NewPinCodeDebitCardDto newPinCodeDebitCardDto;
    private CardInfoDto cardInfoDto;
    private CreatePaymentDepositDto createPaymentDepositDto;

    @BeforeAll
    public void setUp() {
        LocalDate now = LocalDate.now();
        List<CardDto> cardDtoList = new ArrayList<>();
        cardDtoList.add(CardDto.builder()
                .cardId(UUID.randomUUID())
                .cardNumber("1337")
                .transactionLimit(BigDecimal.TEN)
                .status(CardStatus.ACTIVE)
                .expirationDate(now.plusYears(1L))
                .holderName("holderName")
                .digitalWallet(DigitalWallet.APPLEPAY)
                .isDefault(true)
                .cardProductId(1)
                .cardName("VISA")
                .paymentSystem(PaymentSystem.MASTERCARD).build());

        accountWithCardInfoDtoList = new ArrayList<>();

        accountWithCardInfoDtoList.add(AccountWithCardInfoDto.builder()
                .cardId(CARD_ID)
                .cardNumber("1337")
                .expirationDate(now.plusYears(1L))
                .cardName("VISA")
                .paymentSystem(PaymentSystem.MASTERCARD)
                .cardBalance(BigDecimal.ONE)
                .currencyCode(CurrencyCode.RUB).build());

        viewCardDto = ViewCardDto.builder()
                .cardName("1337")
                .build();

        cardStatusDto = CardStatusDto.builder()
                .cardStatus(CardStatus.BLOCKED)
                .build();

        cardDebitLimitDto = CardDebitLimitDto.builder()
                .cardNumber("1337")
                .transactionLimit(BigDecimal.TEN)
                .build();

        cardNumberDto = CardNumberDto.builder()
                .cardNumber("12356545")
                .build();

        newPinCodeDebitCardDto = NewPinCodeDebitCardDto.builder()
                .cardNumber("55455")
                .newPin("5855")
                .build();

        cardInfoDto = CardInfoDto.builder()
                .holderName("holder_name")
                .status(CardStatus.ACTIVE)
                .transactionLimit(new BigDecimal(700))
                .build();

        createPaymentDepositDto = CreatePaymentDepositDto.builder()
                .remitterCardNumber("0000111100001111")
                .sum(BigDecimal.valueOf(300.0))
                .build();
    }

    @Test
    @DisplayName("If the list of active accounts with cards was successfully received then return status OK")
    void getClientCurrentAccountsWithCards_ifSuccess_thenStatus200() throws Exception {
        //ARRANGE
        when(accountService.getActiveAccountsWithCard(CLIENT_ID)).thenReturn(accountWithCardInfoDtoList);

        //ACT & VERIFY
        MvcResult result = mockMvc.perform(
                        get(CardController.URL_CARDS)
                                .param(CardController.PARAM_CLIENT_ID, CLIENT_ID.toString()))
                .andExpect(status().isOk())
                .andReturn();
        verifyBody(asJsonString(accountWithCardInfoDtoList), result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("If the list of current accounts with cards wasn't successfully received then return status INTERNAL SERVER ERROR")
    void getClientCurrentAccountsWithCard_ifNotSuccess_thenStatus500() throws Exception {
        //ARRANGE
        when(accountService.getActiveAccountsWithCard(CLIENT_ID)).thenThrow(new RuntimeException());

        //ACT & VERIFY
        mockMvc.perform(
                        get(CardController.URL_CARDS)
                                .param(CardController.PARAM_CLIENT_ID, CLIENT_ID.toString()))
                .andExpect(status().isInternalServerError())
                .andReturn();
    }

    @Test
    @DisplayName("if card was successfully found then return status OK")
    void getAccountByCardId_shouldReturnAccountNumberDto() throws Exception {
        //ARRANGE
        when(accountService.getViewCardByCardId(any(UUID.class), any(UUID.class))).thenReturn(viewCardDto);

        //ACT
        MvcResult result = mockMvc.perform(get(CardController.URL_CARDS + CardController.URL_CARD_ID, CARD_ID.toString())
                        .param("clientId", CLIENT_ID.toString()))
                .andExpect(status().isOk())
                .andReturn();

        //VERIFY
        verifyBody(asJsonString(viewCardDto), result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("if card wasn't successfully found then return status INTERNAL_SERVER_ERROR")
    void getAccountByCardId_ifNotSuccess_then500_INTERNAL_SERVER_ERROR() throws Exception {
        //ARRANGE
        when(accountService.getViewCardByCardId(any(UUID.class), any(UUID.class))).thenThrow(RuntimeException.class);

        //ACT & VERIFY
        mockMvc.perform(get(CardController.URL_CARDS + CardController.URL_CARD_ID, CARD_ID.toString()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("If successfully changed status then return OK")
    void changeCardStatus_shouldReturnCardStatus() throws Exception {
        //ACT & VERIFY
        mockMvc.perform(
                        patch(CardController.URL_CARDS + CardController.URL_ACTIVE_CARDS)
                                .param("clientId", CLIENT_ID.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(cardStatusDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("If changing status wasn't successfully received then return Internal Server Error")
    void changeCardStatus_ifNotSuccess_thenStatus500() throws Exception {
        //ARRANGE
        doThrow(RuntimeException.class)
                .when(cardService).changeCardStatus(any(UUID.class), any(CardStatusDto.class));

        //ACT & VERIFY
        mockMvc.perform(patch(CardController.URL_CARDS + CardController.URL_ACTIVE_CARDS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(cardStatusDto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("If changing status wasn't successfully received then return Bad Request")
    void changeCardStatus_ifNotSuccess_thenStatus400() throws Exception {
        //ARRANGE
        doThrow(new CardStatusesAreEqualsException(
                Integer.toString(HttpStatus.BAD_REQUEST.value()),
                "The same card status already exists!")).when(cardService)
                .changeCardStatus(any(UUID.class), any(CardStatusDto.class));
        //ACT & VERIFY
        mockMvc.perform(patch(CardController.URL_CARDS + CardController.URL_ACTIVE_CARDS)
                        .param("clientId", CLIENT_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(cardStatusDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("If successfully changed status then return OK")
    void changeDebitCardLimit_shouldReturnDebitCardLimit() throws Exception {
        //ACT & VERIFY
        mockMvc.perform(
                        patch(CardController.URL_CARDS + CardController.URL_LIMIT)
                                .param("clientId", CLIENT_ID.toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(cardDebitLimitDto)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("If changing status wasn't successfully received then return Internal Server Error")
    void changeDebitCardLimit_ifNotSuccess_thenStatus500() throws Exception {
        //ARRANGE
        doThrow(RuntimeException.class)
                .when(cardService).changeDebitCardLimit(any(UUID.class), any(CardDebitLimitDto.class));

        //ACT & VERIFY
        mockMvc.perform(patch(CardController.URL_CARDS + CardController.URL_LIMIT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(cardDebitLimitDto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("If debit card was successfully delete then return status No_Content")
    void deleteDebitCard_ifSuccessfullyDeleted_then204_NO_CONTENT() throws Exception {
        //ARRANGE
        ArgumentCaptor<UUID> operationIdCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<UUID> clientIdCaptor = ArgumentCaptor.forClass(UUID.class);

        //ACT
        ResultActions perform = mockMvc.perform(
                delete(CardController.URL_CARDS + CardController.URL_CARD_ID, CARD_ID)
                        .param("clientId", CLIENT_ID.toString())
        );

        //VERIFY
        perform.andExpect(status().isNoContent());
        verify(cardService, times(1)).deleteDebitCard(clientIdCaptor.capture(), operationIdCaptor.capture());
        assertThat(CARD_ID).isEqualTo(operationIdCaptor.getValue());
        assertThat(CLIENT_ID).isEqualTo(clientIdCaptor.getValue());
    }

    @Test
    @DisplayName("If debit card wasn't successfully delete then return status BAD_REQUEST")
    void deleteDebitCard_ifNotDeleted_then400_BAD_REQUEST() throws Exception {
        //ARRANGE
        doThrow(EntityNotFoundException.class).when(cardService).deleteDebitCard(any(UUID.class), any(UUID.class));

        //ACT
        ResultActions perform = mockMvc.perform(
                delete(CardController.URL_CARDS + CardController.URL_CARD_ID, CARD_ID)
                        .param("clientId", CLIENT_ID.toString())
        );

        //VERIFY
        perform.andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("if card was successfully found then return status OK")
    void getCardNumberByCardId_shouldReturnAccountNumberDto() throws Exception {
        //ARRANGE
        when(cardService.getCardNumberByCardId(any(UUID.class))).thenReturn(cardNumberDto);

        //ACT
        MvcResult result = mockMvc.perform(get(CardController.URL_CARDS + CardController
                        .URL_CARD_INFORMATION, CARD_ID.toString()))
                .andExpect(status().isOk())
                .andReturn();

        //VERIFY
        verifyBody(asJsonString(cardNumberDto), result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("if card wasn't successfully found then return status INTERNAL_SERVER_ERROR")
    void getCardNumberByCardId_ifNotSuccess_then500_INTERNAL_SERVER_ERROR() throws Exception {
        //ARRANGE
        when(cardService.getCardNumberByCardId(any(UUID.class))).thenThrow(RuntimeException.class);

        //ACT & VERIFY
        mockMvc.perform(get(CardController.URL_CARDS + CardController.URL_CARD_INFORMATION, CARD_ID.toString()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("If successfully change pin-code debit card then return status OK")
    void changePinCodeDebitCard_shouldNotReturnContent() throws Exception {
        //ACT
        MvcResult result = mockMvc.perform(post(
                        CardController.URL_CARDS + CardController.URL_CARD_PIN_CODE)
                        .param("clientId", CLIENT_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(newPinCodeDebitCardDto)))
                .andExpect(status().isOk())
                .andReturn();

        //VERIFY
        verifyBody(asJsonString(newPinCodeDebitCardDto), new String(Objects.requireNonNull(result.getRequest().getContentAsByteArray())));
    }

    @Test
    @DisplayName("If change pin-code debit card failed then return InternalServerError")
    void changePinCodeDebitCard_ifNotSent_then500_InternalServerError() throws Exception {
        //ARRANGE
        doThrow(RuntimeException.class).when(cardService).changePinCodeDebitCard(any(UUID.class), any(NewPinCodeDebitCardDto.class));

        //ACT & VERIFY
        ResultActions perform = mockMvc.perform(post(
                CardController.URL_CARDS + CardController.URL_CARD_PIN_CODE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(newPinCodeDebitCardDto)));
        perform.andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("if card was successfully found then return status OK")
    void getCardInformation_shouldReturnCardInfoDto() throws Exception {
        //ARRANGE
        when(cardService.getCardInfo(any(UUID.class), any(UUID.class))).thenReturn(cardInfoDto);

        //ACT
        MvcResult result = mockMvc.perform(
                        get("/auth/deposit-cards/{cardId}/info", CARD_ID.toString())
                                .param("clientId", CLIENT_ID.toString()))
                .andExpect(status().isOk())
                .andReturn();

        //VERIFY
        verifyBody(asJsonString(cardInfoDto), result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("if card wasn't successfully found then return status INTERNAL_SERVER_ERROR")
    void getCardInformation_ifNotFoundCard_then500_INTERNAL_SERVER_ERROR() throws Exception {
        //ARRANGE
        when(cardService.getCardInfo(any(UUID.class), any(UUID.class))).thenThrow(RuntimeException.class);

        //ACT & VERIFY
        mockMvc.perform(
                        get("/auth/deposit-cards/{cardId}/info", CARD_ID.toString()))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("if sum was successfully write off return status OK")
    void writeOffSum_shouldReturnTrue() throws Exception {
        //ARRANGE
        when(cardService.writeOffSum(any(UUID.class), any(CreatePaymentDepositDto.class))).thenReturn(true);

        //ACT
        MvcResult result = mockMvc.perform(
                        patch(URL_WRITE_OFF_SUM + CLIENT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(createPaymentDepositDto)))
                .andExpect(status().isOk())
                .andReturn();

        //VERIFY
        verifyBody(asJsonString(true), result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("if card wasn't successfully found then return status INTERNAL_SERVER_ERROR")
    void writeOffSum_ifNotFoundCard_then400_BAD_REQUEST() throws Exception {
        //ARRANGE
        when(cardService.writeOffSum(any(UUID.class), any(CreatePaymentDepositDto.class)))
                .thenThrow(new EntityNotFoundException("Card with card number " +
                        createPaymentDepositDto.getRemitterCardNumber() + " wasn't found"));

        //ACT & VERIFY
        mockMvc.perform(
                        patch(URL_WRITE_OFF_SUM + CLIENT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(createPaymentDepositDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("if card wasn't successfully found then return status INTERNAL_SERVER_ERROR")
    void writeOffSum_ifNotFoundCard_then500_INTERNAL_SERVER_ERROR() throws Exception {
        //ARRANGE
        when(cardService.writeOffSum(any(UUID.class), any(CreatePaymentDepositDto.class)))
                .thenThrow(new RuntimeException());

        //ACT & VERIFY
        mockMvc.perform(
                        patch(URL_WRITE_OFF_SUM + CLIENT_ID)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(asJsonString(createPaymentDepositDto)))
                .andExpect(status().isInternalServerError());
    }

    private static String asJsonString(final Object obj) throws JsonProcessingException {
        return new ObjectMapper()
                .setDateFormat(new SimpleDateFormat(FORMAT_DATE))
                .registerModule(new JavaTimeModule())
                .writeValueAsString(obj);
    }

    private void verifyBody(String expectedBody, String actualBody) {
        assertEquals(actualBody, expectedBody);
    }
}