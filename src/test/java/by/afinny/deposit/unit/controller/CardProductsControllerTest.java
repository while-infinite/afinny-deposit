package by.afinny.deposit.unit.controller;

import by.afinny.deposit.controller.CardProductsController;
import by.afinny.deposit.dto.CardProductDto;
import by.afinny.deposit.entity.constant.CoBrand;
import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.entity.constant.PaymentSystem;
import by.afinny.deposit.entity.constant.PremiumStatus;
import by.afinny.deposit.exception.handler.ExceptionHandlerController;
import by.afinny.deposit.service.CardProductsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardProductsController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class CardProductsControllerTest {

    @MockBean
    private CardProductsService cardProductsService;

    private static final String FORMAT_DATE = "yyyy-MM-dd";

    private MockMvc mockMvc;
    private List<CardProductDto> cardProductDtoList;

    @BeforeAll
    public void setUp() {

        mockMvc = MockMvcBuilders.standaloneSetup(new CardProductsController(cardProductsService))
                .setControllerAdvice(new ExceptionHandlerController())
                .build();

        cardProductDtoList = List.of(
                CardProductDto.builder()
                        .cardName("TEST")
                        .paymentSystem(PaymentSystem.VISA)
                        .coBrand(CoBrand.AEROFLOT)
                        .isVirtual(Boolean.FALSE)
                        .premiumStatus(PremiumStatus.CLASSIC)
                        .servicePrice(BigDecimal.valueOf(0))
                        .productPrice(BigDecimal.valueOf(0))
                        .currencyCode(CurrencyCode.RUB)
                        .isActive(Boolean.TRUE)
                        .cardDuration(5).build());
    }



    @Test
    @DisplayName("if card products was found then return status OK")
    void getAllCardProducts_shouldReturnStatusOK() throws Exception {
        //ARRANGE
        when(cardProductsService.getAllCardProducts()).thenReturn(cardProductDtoList);

        //ACT
        ResultActions perform = mockMvc.perform(
                        get("/auth/cards-products"))
                .andExpect(status().isOk());

        //VERIFY
        MvcResult result = perform.andReturn();
        verifyBody(asJsonString(cardProductDtoList), result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("if DB error has occurred then return status INTERNAL SERVER ERROR")
    void getAllCardProducts_ifDbError_thenReturnStatusINTERNAL_SERVER_ERROR() throws Exception {
        //ARRANGE
        when(cardProductsService.getAllCardProducts()).thenThrow(new RuntimeException());

        //ACT&VERIFY
        mockMvc.perform(get("/auth/cards-products"))
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
