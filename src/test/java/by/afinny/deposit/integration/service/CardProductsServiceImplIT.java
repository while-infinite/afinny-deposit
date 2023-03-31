package by.afinny.deposit.integration.service;

import by.afinny.deposit.dto.CardProductDto;
import by.afinny.deposit.dto.ProductDto;
import by.afinny.deposit.entity.CardProduct;
import by.afinny.deposit.entity.constant.CoBrand;
import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.entity.constant.PaymentSystem;
import by.afinny.deposit.entity.constant.PremiumStatus;
import by.afinny.deposit.integration.config.annotation.TestWithPostgresContainer;
import by.afinny.deposit.mapper.CardProductMapper;
import by.afinny.deposit.repository.CardProductRepository;
import by.afinny.deposit.service.CardProductsService;
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
@DisplayName("CardProductServiceIT")
public class CardProductsServiceImplIT {

    private CardProduct cardProduct;
    @SpyBean
    private CardProductsService cardProductsService;
    @SpyBean
    private CardProductRepository cardProductRepository;
    @SpyBean
    private CardProductMapper cardProductMapper;

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
    void getCardProducts_shouldReturnCardProducts() {
        //ARRANGE
        cardProduct.setIsActive(Boolean.TRUE);
        cardProductRepository.save(cardProduct);
        //ACT
        List<CardProductDto> productDtoResult = cardProductsService.getAllCardProducts();
        List<CardProductDto> productDtoActual = cardProductMapper.toCardProductDtoList(cardProductRepository.findAllByIsActiveTrue());
        //VERIFY
        verifyCardProducts(productDtoResult, productDtoActual);
    }

    @Test
    @DisplayName("Should return empty products list in case no active products")
    void getProducts_shouldReturnEmptyProducts() {
        //ARRANGE
        cardProduct.setIsActive(Boolean.FALSE);
        cardProductRepository.save(cardProduct);
        //ACT
        List<CardProductDto> productDtoResult = cardProductsService.getAllCardProducts();
        //VERIFY
        assertThat(productDtoResult.isEmpty())
                .isTrue();
    }

    private void verifyCardProducts(List<CardProductDto> result, List<CardProductDto> actual) {
        assertThat(result)
                .usingRecursiveComparison()
                .isEqualTo(actual);
    }
}
