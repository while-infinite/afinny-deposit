package by.afinny.deposit.unit.mapper;

import by.afinny.deposit.dto.ProductDto;
import by.afinny.deposit.entity.Product;
import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.entity.constant.SchemaName;
import by.afinny.deposit.mapper.ProductMapperImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
class ProductMapperTest {

    @InjectMocks
    private ProductMapperImpl productMapper;

    private Product product;
    private List<ProductDto> productDtoList;

    @BeforeAll
    void setUp() {
        product = Product.builder()
                .id(1)
                .name("product name")
                .interestRateEarly(new BigDecimal(10))
                .isCapitalization(true)
                .amountMin(new BigDecimal(100))
                .amountMax(new BigDecimal(1000000))
                .isRevocable(true)
                .minInterestRate(new BigDecimal(1))
                .maxInterestRate(new BigDecimal(20))
                .minDurationMonths(1)
                .maxDurationMonths(100)
                .schemaName(SchemaName.FIXED)
                .currencyCode(CurrencyCode.EUR).build();
    }

    @Test
    @DisplayName("Verify product dto fields setting")
    void productsToProductsDto_shouldReturnCorrectMappingData() {
        //ACT
        productDtoList = productMapper.productsToProductsDto(List.of(product));
        //VERIFY
        verifyProductFields();
    }

    private void verifyProductFields() {
        ProductDto productDto = productDtoList.get(0);
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(productDtoList).hasSize(1);
            softAssertions.assertThat(productDto.getId()).isEqualTo(product.getId());
            softAssertions.assertThat(productDto.getName()).isEqualTo(product.getName());
            softAssertions.assertThat(productDto.getInterestRateEarly()).isEqualTo(product.getInterestRateEarly());
            softAssertions.assertThat(productDto.getIsCapitalization()).isEqualTo(product.getIsCapitalization());
            softAssertions.assertThat(productDto.getAmountMin()).isEqualTo(product.getAmountMin());
            softAssertions.assertThat(productDto.getAmountMax()).isEqualTo(product.getAmountMax());
            softAssertions.assertThat(productDto.getIsRevocable()).isEqualTo(product.getIsRevocable());
            softAssertions.assertThat(productDto.getMinInterestRate()).isEqualTo(product.getMinInterestRate());
            softAssertions.assertThat(productDto.getMaxInterestRate()).isEqualTo(product.getMaxInterestRate());
            softAssertions.assertThat(productDto.getMinDurationMonths()).isEqualTo(product.getMinDurationMonths());
            softAssertions.assertThat(productDto.getMaxDurationMonths()).isEqualTo(product.getMaxDurationMonths());
            softAssertions.assertThat(productDto.getSchemaName()).isEqualTo(product.getSchemaName());
            softAssertions.assertThat(productDto.getCurrencyCode()).isEqualTo(product.getCurrencyCode());
        });
    }
}