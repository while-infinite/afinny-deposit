package by.afinny.deposit.unit.mapper;

import by.afinny.deposit.dto.CardProductDto;
import by.afinny.deposit.entity.CardProduct;
import by.afinny.deposit.entity.constant.CoBrand;
import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.entity.constant.PaymentSystem;
import by.afinny.deposit.entity.constant.PremiumStatus;
import by.afinny.deposit.mapper.CardProductMapperImpl;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

@ExtendWith({MockitoExtension.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ActiveProfiles("test")
@DisplayName("CardProductMapperTest")
class CardProductMapperTest {

    @InjectMocks
    private CardProductMapperImpl cardProductMapper;

    private List<CardProduct> cardProducts;
    private List<CardProductDto> cardProductDtoList;

    @BeforeEach
    void setUp() {
        cardProducts = List.of(
                CardProduct.builder()
                        .cardName("TEST")
                        .paymentSystem(PaymentSystem.VISA)
                        .coBrand(CoBrand.AEROFLOT)
                        .isVirtual(Boolean.FALSE)
                        .premiumStatus(PremiumStatus.CLASSIC)
                        .servicePrice(BigDecimal.valueOf(0))
                        .productPrice(BigDecimal.valueOf(0))
                        .currencyCode(CurrencyCode.RUB)
                        .isActive(Boolean.TRUE)
                        .cardDuration(5).build());
    }

    @Test
    @DisplayName("Verify card product dto fields setting")
    void toCardProductDtoList_shouldReturnCorrectMappingData() {
        // ACT
        cardProductDtoList = cardProductMapper.toCardProductDtoList(cardProducts);
        //VERIFY
        assertSoftly(softAssertions -> {
            softAssertions.assertThat(cardProducts.stream().map(CardProduct::getId).collect(Collectors.toList()))
                    .withFailMessage("Ids should be equals")
                    .isEqualTo(cardProductDtoList.stream().map(CardProductDto::getId).collect(Collectors.toList()));
            softAssertions.assertThat(cardProducts.stream().map(CardProduct::getCardName).collect(Collectors.toList()))
                    .withFailMessage("Card names should be equals")
                    .isEqualTo(cardProductDtoList.stream().map(CardProductDto::getCardName).collect(Collectors.toList()));
            softAssertions.assertThat(cardProducts.stream().map(CardProduct::getPaymentSystem).collect(Collectors.toList()))
                    .withFailMessage("Payment systems should be equals")
                    .isEqualTo(cardProductDtoList.stream().map(CardProductDto::getPaymentSystem).collect(Collectors.toList()));
            softAssertions.assertThat(cardProducts.stream().map(CardProduct::getCashback).collect(Collectors.toList()))
                    .withFailMessage("Cashbacks should be equals")
                    .isEqualTo(cardProductDtoList.stream().map(CardProductDto::getCashback).collect(Collectors.toList()));
            softAssertions.assertThat(cardProducts.stream().map(CardProduct::getCoBrand).collect(Collectors.toList()))
                    .withFailMessage("CoBrands should be equals")
                    .isEqualTo(cardProductDtoList.stream().map(CardProductDto::getCoBrand).collect(Collectors.toList()));
            softAssertions.assertThat(cardProducts.stream().map(CardProduct::getIsVirtual).collect(Collectors.toList()))
                    .withFailMessage("Virtual status should be equals")
                    .isEqualTo(cardProductDtoList.stream().map(CardProductDto::getIsVirtual).collect(Collectors.toList()));
            softAssertions.assertThat(cardProducts.stream().map(CardProduct::getPremiumStatus).collect(Collectors.toList()))
                    .withFailMessage("Premium statuses should be equals")
                    .isEqualTo(cardProductDtoList.stream().map(CardProductDto::getPremiumStatus).collect(Collectors.toList()));
            softAssertions.assertThat(cardProducts.stream().map(CardProduct::getServicePrice).collect(Collectors.toList()))
                    .withFailMessage("Service prices should be equals")
                    .isEqualTo(cardProductDtoList.stream().map(CardProductDto::getServicePrice).collect(Collectors.toList()));
            softAssertions.assertThat(cardProducts.stream().map(CardProduct::getProductPrice).collect(Collectors.toList()))
                    .withFailMessage("Product prices should be equals")
                    .isEqualTo(cardProductDtoList.stream().map(CardProductDto::getProductPrice).collect(Collectors.toList()));
            softAssertions.assertThat(cardProducts.stream().map(CardProduct::getCurrencyCode).collect(Collectors.toList()))
                    .withFailMessage("Currency codes should be equals")
                    .isEqualTo(cardProductDtoList.stream().map(CardProductDto::getCurrencyCode).collect(Collectors.toList()));
            softAssertions.assertThat(cardProducts.stream().map(CardProduct::getIsActive).collect(Collectors.toList()))
                    .withFailMessage("Active statuses should be equals")
                    .isEqualTo(cardProductDtoList.stream().map(CardProductDto::getIsActive).collect(Collectors.toList()));
            softAssertions.assertThat(cardProducts.stream().map(CardProduct::getCardDuration).collect(Collectors.toList()))
                    .withFailMessage("Card durations should be equals")
                    .isEqualTo(cardProductDtoList.stream().map(CardProductDto::getCardDuration).collect(Collectors.toList()));
            softAssertions.assertAll();
        });
    }
}