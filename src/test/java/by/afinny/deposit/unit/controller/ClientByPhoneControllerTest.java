package by.afinny.deposit.unit.controller;

import by.afinny.deposit.controller.ClientByPhoneController;
import by.afinny.deposit.dto.ClientDto;
import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.exception.EntityNotFoundException;
import by.afinny.deposit.exception.handler.ExceptionHandlerController;
import by.afinny.deposit.service.ClientService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@WebMvcTest(ClientByPhoneController.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ClientByPhoneControllerTest {

    @MockBean
    private ClientService clientService;

    private final UUID CLIENT_ID = UUID.randomUUID();
    private final String FIRST_NAME = "Ivan";
    private final String LAST_NAME = "Ivanov";
    private final String MIDDLE_NAME = "Ivanovich";
    private final String ACCOUNT_NUMBER = "accountNumber";
    private final String MOBILE_PHONE = "+79999999999";
    private final CurrencyCode CURRENCY_CODE = CurrencyCode.RUB;

    private MockMvc mockMvc;
    private ClientDto clientDto;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ClientByPhoneController(clientService))
                .setControllerAdvice(ExceptionHandlerController.class).build();

        clientDto = ClientDto.builder()
                .clientId(CLIENT_ID)
                .firstName(FIRST_NAME)
                .middleName(MIDDLE_NAME)
                .lastName(LAST_NAME)
                .accountNumber(ACCOUNT_NUMBER)
                .build();
    }

    @Test
    @DisplayName("If client point correct phone and currency code, then return  OK and client dto")
    void getClientByPhone_ifClientFound_thenReturnOkAndReturnClientDto() throws Exception {
        //Arrange
        Mockito.when(clientService.getClientByPhoneNumber(CLIENT_ID, CURRENCY_CODE, MOBILE_PHONE)).thenReturn(clientDto);
        //ACT
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/auth/accounts")
                        .param("clientId", CLIENT_ID.toString())
                        .param("mobilePhone", MOBILE_PHONE)
                        .param("currency_code", CURRENCY_CODE.name()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();
        //VERIFY
        verifyBody(asJsonString(clientDto), result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("If client point incorrect phone, then throw entity not found exception")
    void getClientByPhone_ifClientFillIncorrectPhoneOrCurrencyCode_thenReturnBadRequest() throws Exception {
        //Arrange
        Mockito.when(clientService.getClientByPhoneNumber(CLIENT_ID, CURRENCY_CODE, MOBILE_PHONE)).thenThrow(EntityNotFoundException.class);
        //ACT&VERIFY
        mockMvc.perform(MockMvcRequestBuilders.get("/auth/accounts")
                        .param("clientId", CLIENT_ID.toString())
                        .param("mobilePhone", MOBILE_PHONE)
                        .param("currency_code", CURRENCY_CODE.name()))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    private void verifyBody(String expectedBody, String actualBody) {
        assertThat(actualBody).isEqualTo(expectedBody);
    }

    private String asJsonString(Object obj) throws JsonProcessingException {
        return new ObjectMapper().findAndRegisterModules().writeValueAsString(obj);
    }

}
