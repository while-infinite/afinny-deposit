package by.afinny.deposit.unit.controller;

import by.afinny.deposit.controller.ProductController;
import by.afinny.deposit.dto.ProductDto;
import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.entity.constant.SchemaName;
import by.afinny.deposit.exception.handler.ExceptionHandlerController;
import by.afinny.deposit.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.TestInstance.Lifecycle;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@WebMvcTest(ProductController.class)
@TestInstance(Lifecycle.PER_CLASS)
class ProductControllerTest {

    @MockBean
    private ProductService productService;

    private ProductDto productDto;
    private List<ProductDto> productDtoList;
    private MockMvc mockMvc;

    @BeforeAll
    void setUp() {
        mockMvc = standaloneSetup(new ProductController(productService))
                .setControllerAdvice(new ExceptionHandlerController()).build();

        productDto = ProductDto.builder()
                .name("Product #1")
                .id(1)
                .minInterestRate(new BigDecimal(10))
                .maxInterestRate(new BigDecimal(12))
                .interestRateEarly(new BigDecimal(9))
                .currencyCode(CurrencyCode.RUB)
                .isRevocable(true)
                .schemaName(SchemaName.FIXED)
                .isCapitalization(true)
                .minDurationMonths(10)
                .maxDurationMonths(20)
                .amountMin(new BigDecimal(1))
                .amountMax(new BigDecimal(100000))
                .build();
        productDtoList = List.of(productDto);
    }

    @Test
    @DisplayName("If products was successfully found then return list of products")
    void getActiveDepositProducts_shouldReturnListProducts() throws Exception {
        //ARRANGE
        when(productService.getActiveDepositProducts()).thenReturn(productDtoList);
        //ACT & VERIFY
        MvcResult result = mockMvc.perform(get("/auth/deposit-products"))
                .andExpect(status().isOk())
                .andReturn();
        verifyBody(result.getResponse().getContentAsString(), asJsonString(productDtoList));
    }

    @Test
    @DisplayName("If the list of products wasn't successfully received then return status INTERNAL SERVER ERROR")
    void getActiveDepositProducts_ifNotSuccess_thenStatus500() throws Exception {
        //ARRANGE
        when(productService.getActiveDepositProducts()).thenThrow(new RuntimeException());
        //ACT & VERIFY
        mockMvc.perform(get("/auth/deposit-products"))
                .andExpect(status().isInternalServerError());
    }

    private static String asJsonString(final Object obj) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(obj);
    }

    private void verifyBody(String actualBody, String expectedBody) {
        assertThat(actualBody).isEqualTo(expectedBody);
    }
}