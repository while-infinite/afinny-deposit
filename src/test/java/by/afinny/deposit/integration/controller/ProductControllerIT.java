package by.afinny.deposit.integration.controller;

import by.afinny.deposit.dto.ProductDto;
import by.afinny.deposit.entity.Product;
import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.entity.constant.SchemaName;
import by.afinny.deposit.integration.config.annotation.TestWithPostgresContainer;
import by.afinny.deposit.mapper.ProductMapper;
import by.afinny.deposit.repository.ProductRepository;
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
@DisplayName("ProductControllerIT")
public class ProductControllerIT {

    private Product product;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private MappingUtils mappingUtils;

    @BeforeAll
    void setUp() {
        product = Product.builder()
                .name("Product #1")
                .id(1)
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

    @Test
    @DisplayName("Should return active products list from test container")
    void getProducts_shouldReturnProducts() throws Exception {
        //ARRANGE
        product.setIsActive(true);
        productRepository.save(product);
        //ACT
        MvcResult result = mockMvc.perform(get("/auth/deposit-products"))
                .andExpect(status().isOk())
                .andReturn();
        List<ProductDto> productDtoResult = mappingUtils.getObjectListFromJson(result.getResponse().getContentAsString(), ProductDto.class);
        List<ProductDto> productDtoDB = productMapper.productsToProductsDto(productRepository.findAll());
        //VERIFY
        verifyProducts(productDtoResult, productDtoDB);
    }

    @Test
    @DisplayName("Should return empty products list in case no active products")
    void getProducts_shouldReturnEmptyProducts() throws Exception {
        //ARRANGE
        product.setIsActive(false);
        productRepository.save(product);
        //ACT
        MvcResult result = mockMvc.perform(get("/auth/deposit-products"))
                .andExpect(status().isOk())
                .andReturn();
        List<ProductDto> productDtoResult = mappingUtils.getObjectListFromJson(result.getResponse().getContentAsString(), ProductDto.class);
        //VERIFY
        assertThat(productDtoResult.isEmpty())
                .isTrue();
    }

    private void verifyProducts(List<ProductDto> result, List<ProductDto> actual) {
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(actual);
    }
}
