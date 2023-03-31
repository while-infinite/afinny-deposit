package by.afinny.deposit.integration.service;

import by.afinny.deposit.dto.ProductDto;
import by.afinny.deposit.entity.Product;
import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.entity.constant.SchemaName;
import by.afinny.deposit.integration.config.annotation.TestWithPostgresContainer;
import by.afinny.deposit.mapper.ProductMapper;
import by.afinny.deposit.repository.ProductRepository;
import by.afinny.deposit.service.ProductService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.jdbc.Sql;
import java.math.BigDecimal;
import java.util.List;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@TestWithPostgresContainer
@Sql(scripts = "/truncate.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DisplayName("ProductServiceIT")
public class ProductServiceImplIT {

    private Product product;
    @SpyBean
    private ProductService productService;
    @SpyBean
    private ProductRepository productRepository;
    @SpyBean
    private ProductMapper productMapper;

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
    void getProducts_shouldReturnProducts() {
        //ARRANGE
        product.setIsActive(Boolean.TRUE);
        productRepository.save(product);
        //ACT
        List<ProductDto> productDtoResult = productService.getActiveDepositProducts();
        List<ProductDto> productDtoActual = productMapper.productsToProductsDto(productRepository.findByIsActiveTrue());
        //VERIFY
        verifyProducts(productDtoResult, productDtoActual);
    }

    @Test
    @DisplayName("Should return empty products list in case no active products")
    void getProducts_shouldReturnEmptyProducts() {
        //ARRANGE
        product.setIsActive(Boolean.FALSE);
        productRepository.save(product);
        //ACT
        List<ProductDto> productDtoResult = productService.getActiveDepositProducts();
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
