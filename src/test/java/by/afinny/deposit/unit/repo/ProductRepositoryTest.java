package by.afinny.deposit.unit.repo;

import by.afinny.deposit.entity.Product;
import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.entity.constant.SchemaName;
import by.afinny.deposit.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Sql(
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        scripts = {"/schema-h2.sql"}
)
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    private Product activeProduct;
    private Product notActiveProduct;

    @BeforeAll
    void setUp() {
        activeProduct = Product.builder()
                .name("product name")
                .interestRateEarly(new BigDecimal("10.00"))
                .isCapitalization(true)
                .amountMin(new BigDecimal("100.00"))
                .amountMax(new BigDecimal("1000000.00"))
                .isRevocable(true)
                .isActive(true)
                .minInterestRate(new BigDecimal("1.00"))
                .maxInterestRate(new BigDecimal("20.00"))
                .minDurationMonths(1)
                .maxDurationMonths(100)
                .schemaName(SchemaName.FIXED)
                .currencyCode(CurrencyCode.EUR).build();
        notActiveProduct = Product.builder()
                .name("product name")
                .interestRateEarly(new BigDecimal("10.00"))
                .isCapitalization(true)
                .amountMin(new BigDecimal("100.00"))
                .amountMax(new BigDecimal("1000000.00"))
                .isRevocable(true)
                .isActive(false)
                .minInterestRate(new BigDecimal("1.00"))
                .maxInterestRate(new BigDecimal("20.00"))
                .minDurationMonths(1)
                .maxDurationMonths(100)
                .schemaName(SchemaName.FIXED)
                .currencyCode(CurrencyCode.EUR).build();
    }

    @AfterEach
    void cleanUp() {
        productRepository.deleteAll();
    }

    @Test
    @DisplayName("If active products exists then return only active product's list")
    void findByIsActiveTrue_thenReturnProductList() {
        //ARRANGE
        Product savedActiveProduct = productRepository.save(activeProduct);
        activeProduct.setId(savedActiveProduct.getId());
        productRepository.save(notActiveProduct);
        //ACT
        List<Product> productList = productRepository.findByIsActiveTrue();
        //VERIFY
        assertThat(productList).hasSize(1);
        verifyProductFields(productList);
    }

    private void verifyProductFields(List<Product> products) {
        Product foundActiveProduct = products.get(0);
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(foundActiveProduct.getId()).isEqualTo(activeProduct.getId());
            softAssertions.assertThat(foundActiveProduct.getName()).isEqualTo(activeProduct.getName());
            softAssertions.assertThat(foundActiveProduct.getInterestRateEarly()).isEqualTo(activeProduct.getInterestRateEarly());
            softAssertions.assertThat(foundActiveProduct.getIsCapitalization()).isEqualTo(activeProduct.getIsCapitalization());
            softAssertions.assertThat(foundActiveProduct.getAmountMin()).isEqualTo(activeProduct.getAmountMin());
            softAssertions.assertThat(foundActiveProduct.getAmountMax()).isEqualTo(activeProduct.getAmountMax());
            softAssertions.assertThat(foundActiveProduct.getIsRevocable()).isEqualTo(activeProduct.getIsRevocable());
            softAssertions.assertThat(foundActiveProduct.getIsActive()).isEqualTo(activeProduct.getIsActive());
            softAssertions.assertThat(foundActiveProduct.getMinInterestRate()).isEqualTo(activeProduct.getMinInterestRate());
            softAssertions.assertThat(foundActiveProduct.getMaxInterestRate()).isEqualTo(activeProduct.getMaxInterestRate());
            softAssertions.assertThat(foundActiveProduct.getMinDurationMonths()).isEqualTo(activeProduct.getMinDurationMonths());
            softAssertions.assertThat(foundActiveProduct.getMaxDurationMonths()).isEqualTo(activeProduct.getMaxDurationMonths());
            softAssertions.assertThat(foundActiveProduct.getSchemaName()).isEqualTo(activeProduct.getSchemaName());
            softAssertions.assertThat(foundActiveProduct.getCurrencyCode()).isEqualTo(activeProduct.getCurrencyCode());
        });
    }
}