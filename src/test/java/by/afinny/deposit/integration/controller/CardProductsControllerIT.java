package by.afinny.deposit.integration.controller;

import by.afinny.deposit.dto.CardProductDto;
import by.afinny.deposit.entity.CardProduct;
import by.afinny.deposit.entity.constant.*;
import by.afinny.deposit.integration.config.annotation.TestWithPostgresContainer;
import by.afinny.deposit.mapper.CardProductMapper;
import by.afinny.deposit.repository.CardProductRepository;
import by.afinny.deposit.utils.MappingUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.math.BigDecimal;
import java.util.List;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestWithPostgresContainer
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@AutoConfigureMockMvc
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("CardProductsControllerIT")
public class CardProductsControllerIT {

    private CardProduct cardProduct;
    @Autowired
    private CardProductRepository cardProductRepository;
    @Autowired
    private CardProductMapper cardProductMapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MappingUtils mappingUtils;

    @BeforeAll
    void setUp() {
        cardProduct = CardProduct
                .builder()
                .cardName("TEST")
                .paymentSystem(PaymentSystem.VISA)
                .coBrand(CoBrand.AEROFLOT)
                .isVirtual(Boolean.FALSE)
                .premiumStatus(PremiumStatus.CLASSIC)
                .servicePrice(BigDecimal.valueOf(0))
                .productPrice(BigDecimal.valueOf(0))
                .currencyCode(CurrencyCode.RUB)
                .isActive(Boolean.TRUE)
                .cardDuration(5)
                .build();
    }

    @Test
    @DisplayName("Should return active card products list from test container")
    void getCardProducts_shouldReturnCardProducts() throws Exception {
        //ARRANGE
        cardProduct.setIsActive(Boolean.TRUE);
        cardProductRepository.save(cardProduct);
        //ACT
        MvcResult result = mockMvc.perform(get("/auth/cards-products"))
                .andExpect(status().isOk())
                .andReturn();
        List<CardProductDto> productDtoResult = mappingUtils.getObjectListFromJson(result.getResponse().getContentAsString(), CardProductDto.class);
        List<CardProductDto> productDtoDB = cardProductMapper.toCardProductDtoList(cardProductRepository.findAll());
        //VERIFY
        verifyProducts(productDtoResult, productDtoDB);
    }

    private void verifyProducts(List<CardProductDto> result, List<CardProductDto> actual) {
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(actual);
    }
}
