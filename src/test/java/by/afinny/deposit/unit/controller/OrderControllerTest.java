package by.afinny.deposit.unit.controller;

import by.afinny.deposit.controller.OrderController;
import by.afinny.deposit.dto.RequestNewCardDto;
import by.afinny.deposit.exception.handler.ExceptionHandlerController;
import by.afinny.deposit.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@WebMvcTest(OrderController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class OrderControllerTest {

    private final UUID CLIENT_ID = UUID.randomUUID();

    @MockBean
    private OrderService orderService;

    private MockMvc mockMvc;

    private RequestNewCardDto requestNewCardDto;

    @BeforeAll
    public void setUp() {
        mockMvc = standaloneSetup(new OrderController(orderService))
                .setControllerAdvice(new ExceptionHandlerController()).build();

        requestNewCardDto = RequestNewCardDto.builder()
                .productId(1337).build();
    }

    @Test
    @DisplayName("If an order of new card was successfully received then return status OK")
    void orderNewCard_ifSuccess_thenStatus200() throws Exception {
        //ACT & VERIFY
        mockMvc.perform(
                        post(OrderController.URL_CARD_ORDER + OrderController.URL_ORDER_NEW)
                                .param(OrderController.PARAM_CLIENT_ID, CLIENT_ID.toString())
                                .contentType("application/json")
                                .content(asJsonString(requestNewCardDto)))
                .andExpect(status().isOk());

        verify(orderService)
                .orderNewCard(eq(CLIENT_ID), any(RequestNewCardDto.class));
    }

    @Test
    @DisplayName("If an order of new card wasn't successfully received then return status INTERNAL SERVER ERROR")
    void orderNewCard_ifNotSuccess_thenStatus500() throws Exception {
        //ARRANGE
        doThrow(new RuntimeException())
                .when(orderService)
                .orderNewCard(eq(CLIENT_ID), any(RequestNewCardDto.class));

        //ACT & VERIFY
        mockMvc.perform(
                        post(OrderController.URL_CARD_ORDER + OrderController.URL_ORDER_NEW)
                                .param(OrderController.PARAM_CLIENT_ID, CLIENT_ID.toString())
                                .contentType("application/json")
                                .content(asJsonString(requestNewCardDto)))
                .andExpect(status().isInternalServerError());
    }

    private String asJsonString(final Object obj) throws JsonProcessingException {
        return new ObjectMapper()
                .writeValueAsString(obj);
    }
}
