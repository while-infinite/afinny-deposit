package by.afinny.deposit.integration.controller;

import by.afinny.deposit.dto.RequestNewCardDto;
import by.afinny.deposit.entity.Product;
import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.entity.constant.SchemaName;
import by.afinny.deposit.exception.EntityNotFoundException;
import by.afinny.deposit.integration.config.annotation.TestWithPostgresContainer;
import by.afinny.deposit.repository.ProductRepository;
import by.afinny.deposit.utils.MappingUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Stream;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.assertj.core.api.Assertions.assertThat;

@TestWithPostgresContainer
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("OrderControllerIT")
public class OrderControllerIT {

    private RequestNewCardDto requestNewCardDto;
    private Product product;
    private final UUID CLIENT_ID = UUID.randomUUID();
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private MappingUtils mappingUtils;

    @BeforeAll
    void setUp() {
        product = Product.builder()
                .name("Product #1")
                .minInterestRate(new BigDecimal(10))
                .maxInterestRate(new BigDecimal(12))
                .interestRateEarly(new BigDecimal(9))
                .currencyCode(CurrencyCode.RUB)
                .isActive(true)
                .isRevocable(true)
                .isCapitalization(true)
                .schemaName(SchemaName.FIXED)
                .minDurationMonths(10)
                .maxDurationMonths(20)
                .amountMin(new BigDecimal(1))
                .amountMax(new BigDecimal(100000))
                .build();
    }

    @BeforeEach
    void save() {
        product = productRepository.save(product);

        requestNewCardDto = RequestNewCardDto
                .builder()
                .productId(product.getId())
                .build();
    }

    @Test
    @DisplayName("If an order of new card was successfully received then return ok status")
    void orderNewCard_ifSuccess_thenStatusIsOk() throws Exception {
        //ACT & VERIFY
        mockMvc.perform(
                post("/auth/deposit-card-orders/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("clientId", CLIENT_ID.toString())
                        .content(mappingUtils.asJsonString(requestNewCardDto)))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @MethodSource("generateRandomProductID")
    @DisplayName("If product id is incorrect then return unauthorized status")
    void orderNewCard_ifNotSuccess_thenStatusIsBadRequest(Integer productID) throws Exception {
        //ARRANGE
        RequestNewCardDto dto = RequestNewCardDto.builder()
                .productId(productID)
                .build();
        //ACT
        ResultActions resultActions = mockMvc.perform(
                post("/auth/deposit-card-orders/new")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("clientId", CLIENT_ID.toString())
                        .content(mappingUtils.asJsonString(dto)));
        //VERIFY
        resultActions
                .andExpect(status().isBadRequest())
                .andExpect(result -> assertThat(result.getResolvedException()).isInstanceOf(EntityNotFoundException.class));
    }

    private Stream<Arguments> generateRandomProductID() {
        return Stream.of(
                Arguments.of(99),
                Arguments.of(77));
    }
}
