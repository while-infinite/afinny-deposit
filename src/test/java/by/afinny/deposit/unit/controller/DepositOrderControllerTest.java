package by.afinny.deposit.unit.controller;

import by.afinny.deposit.controller.DepositOrderController;
import by.afinny.deposit.dto.RequestNewDepositDto;
import by.afinny.deposit.exception.handler.ExceptionHandlerController;
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

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@WebMvcTest(DepositOrderController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class DepositOrderControllerTest {

    @MockBean
    private DepositService depositService;
    private MockMvc mockMvc;
    private final UUID CLIENT_ID = UUID.randomUUID();
    private RequestNewDepositDto requestNewDepositDto;

    @BeforeAll
    void setUp() {
        mockMvc = standaloneSetup(new DepositOrderController(depositService))
                .setControllerAdvice(new ExceptionHandlerController()).build();

        requestNewDepositDto = RequestNewDepositDto.builder()
                .productId(1)
                .initialAmount(new BigDecimal(10))
                .cardNumber("1")
                .autoRenewal(true)
                .interestRate(new BigDecimal(10))
                .durationMonth(10)
                .build();
    }

    @Test
    @DisplayName("If deposit sent to kafka successfully then return status OK")
    void createNewDeposit_shouldNotReturnContent() throws Exception {
        //ACT & VERIFY
        MvcResult result = mockMvc.perform(post(
                        DepositOrderController.DEPOSIT_ORDER_URL + DepositOrderController.NEW_DEPOSIT_URL)
                .param("clientId", CLIENT_ID.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(asJsonString(requestNewDepositDto)))
                .andExpect(status().isOk())
                .andReturn();
        verifyBody(asJsonString(requestNewDepositDto), new String(Objects.requireNonNull(result.getRequest().getContentAsByteArray())));
    }

    @Test
    @DisplayName("If deposit wasn't successfully sent to kafka then return status INTERNAL_SERVER_ERROR")
    void createNewDeposit_ifNotSent_then500_InternalServerError() throws Exception {
        //ARRANGE
        doThrow(RuntimeException.class).when(depositService).createNewDeposit(any(UUID.class), any(RequestNewDepositDto.class));
        //ACT
        ResultActions perform = mockMvc.perform(post(
                DepositOrderController.DEPOSIT_ORDER_URL + DepositOrderController.NEW_DEPOSIT_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(requestNewDepositDto)));
        //VERIFY
        perform.andExpect(status().isInternalServerError());

    }

    private void verifyBody(String expectedBody, String actualBody) {
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    private static String asJsonString(final Object obj) throws JsonProcessingException {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .writeValueAsString(obj);
    }
}