package by.afinny.deposit.unit.service;

import by.afinny.deposit.dto.ProductDto;
import by.afinny.deposit.entity.Product;
import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.entity.constant.SchemaName;
import by.afinny.deposit.mapper.ProductMapperImpl;
import by.afinny.deposit.repository.ProductRepository;
import by.afinny.deposit.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_METHOD;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(PER_METHOD)
@ActiveProfiles("test")
class ProductServiceTest {

    @InjectMocks
    private ProductServiceImpl depositService;

    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductMapperImpl productMapper;

    private List<Product> productList;
    private List<ProductDto> productDtoList;
    private Product product;
    private ProductDto productDto;

    @BeforeEach
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
        productDto = ProductDto.builder()
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
        productList = List.of(product);
        productDtoList = List.of(productDto);
    }

    @Test
    @DisplayName("Return available deposits when active deposit products exists")
    void getActiveDepositProducts_shouldReturnListProducts() {
        //ARRANGE
        when(productRepository.findByIsActiveTrue()).thenReturn(productList);
        when(productMapper.productsToProductsDto(productList))
                .thenReturn(productDtoList);
        //ACT
        List<ProductDto> resultProductDtoList = depositService.getActiveDepositProducts();
        //VERIFY
        verifyProductDtoFields(resultProductDtoList);
    }

    @Test
    @DisplayName("If not success then throw Runtime Exception")
    void getActiveDepositProducts_ifNotSuccess_thenThrow() {
        //ARRANGE
        when(productRepository.findByIsActiveTrue()).thenThrow(RuntimeException.class);
        //ACT
        ThrowingCallable getActiveDepositProductsMethod = () -> depositService.getActiveDepositProducts();
        //VERIFY
        assertThatThrownBy(getActiveDepositProductsMethod).isInstanceOf(RuntimeException.class);
    }

    private void verifyProductDtoFields(List<ProductDto> productDtoList) {
        ProductDto activeProductDto = productDtoList.get(0);
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(productDtoList)
                    .withFailMessage("List of products shouldn't be null")
                    .isNotNull();
            softAssertions.assertThat(activeProductDto.getId())
                    .withFailMessage("Product ids should be equals")
                    .isEqualTo(productDto.getId());
            softAssertions.assertThat(activeProductDto.getName())
                    .withFailMessage("Product name should be equals")
                    .isEqualTo(productDto.getName());
            softAssertions.assertThat(activeProductDto.getInterestRateEarly())
                    .withFailMessage("Product InterestRateEarly should be equals")
                    .isEqualTo(productDto.getInterestRateEarly());
            softAssertions.assertThat(activeProductDto.getIsCapitalization())
                    .withFailMessage("Product Capitalization should be equals")
                    .isEqualTo(productDto.getIsCapitalization());
            softAssertions.assertThat(activeProductDto.getAmountMin())
                    .withFailMessage("Product AmountMin should be equals")
                    .isEqualTo(productDto.getAmountMin());
            softAssertions.assertThat(activeProductDto.getAmountMax())
                    .withFailMessage("Product AmountMax should be equals")
                    .isEqualTo(productDto.getAmountMax());
            softAssertions.assertThat(activeProductDto.getIsRevocable())
                    .withFailMessage("Product Revocable should be equals")
                    .isEqualTo(productDto.getIsRevocable());
            softAssertions.assertThat(activeProductDto.getMinInterestRate())
                    .withFailMessage("Product Min Interest Rate should be equals")
                    .isEqualTo(productDto.getMinInterestRate());
            softAssertions.assertThat(activeProductDto.getMaxInterestRate())
                    .withFailMessage("Product Max Interest Rate should be equals")
                    .isEqualTo(productDto.getMaxInterestRate());
            softAssertions.assertThat(activeProductDto.getMinDurationMonths())
                    .withFailMessage("Product Min Duration Months should be equals")
                    .isEqualTo(productDto.getMinDurationMonths());
            softAssertions.assertThat(activeProductDto.getMaxDurationMonths())
                    .withFailMessage("Product Max Duration Months should be equals")
                    .isEqualTo(productDto.getMaxDurationMonths());
            softAssertions.assertThat(activeProductDto.getSchemaName())
                    .withFailMessage("Product Schema Name should be equals")
                    .isEqualTo(productDto.getSchemaName());
            softAssertions.assertThat(activeProductDto.getCurrencyCode())
                    .withFailMessage("Product Currency Code should be equals")
                    .isEqualTo(productDto.getCurrencyCode());
        });
    }
}