package by.afinny.deposit.unit.service;

import by.afinny.deposit.dto.CardProductDto;
import by.afinny.deposit.entity.CardProduct;
import by.afinny.deposit.entity.constant.CoBrand;
import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.entity.constant.PaymentSystem;
import by.afinny.deposit.entity.constant.PremiumStatus;
import by.afinny.deposit.mapper.CardProductMapper;
import by.afinny.deposit.repository.CardProductRepository;
import by.afinny.deposit.service.impl.CardProductsServiceImpl;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class CardProductsServiceTest {

    @InjectMocks
    private CardProductsServiceImpl cardService;
    @Mock
    private CardProductRepository cardProductRepository;
    @Mock
    private CardProductMapper cardProductMapper;

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

        cardProductDtoList = List.of(
                CardProductDto.builder()
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
    @DisplayName("if card products was found then return list of card products")
    void getAllCardProducts_shouldReturnListOfCardProducts() {
        //ARRANGE
        when(cardProductRepository.findAllByIsActiveTrue()).thenReturn(cardProducts);
        when(cardProductMapper.toCardProductDtoList(cardProducts)).thenReturn(cardProductDtoList);

        //ACT
        List<CardProductDto> cardProductDroList = cardService.getAllCardProducts();

        //VERIFY
        verifyListCardProducts(cardProductDtoList);
    }

    @Test
    @DisplayName("if DB error has occurred then throw exception")
    void getAllCardProducts_ifDbError_thenThrow() {
        //ARRANGE
        when(cardProductRepository.findAllByIsActiveTrue()).thenThrow(RuntimeException.class);

        //ACT
        ThrowingCallable getAllCardProductsMethodInvocation = () -> cardService.getAllCardProducts();

        //VERIFY
        assertThatThrownBy(getAllCardProductsMethodInvocation).isInstanceOf(RuntimeException.class);
    }

    private void verifyListCardProducts (List<CardProductDto> actualList) {
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(actualList.stream().map(CardProductDto::getId).collect(Collectors.toList()))
                .withFailMessage("Ids should be equals")
                .isEqualTo(cardProductDtoList.stream().map(CardProductDto::getId).collect(Collectors.toList()));
        softAssertions.assertThat(actualList.stream().map(CardProductDto::getCardName).collect(Collectors.toList()))
                .withFailMessage("Card names should be equals")
                .isEqualTo(cardProductDtoList.stream().map(CardProductDto::getCardName).collect(Collectors.toList()));
        softAssertions.assertThat(actualList.stream().map(CardProductDto::getPaymentSystem).collect(Collectors.toList()))
                .withFailMessage("Payment systems should be equals")
                .isEqualTo(cardProductDtoList.stream().map(CardProductDto::getPaymentSystem).collect(Collectors.toList()));
        softAssertions.assertThat(actualList.stream().map(CardProductDto::getCashback).collect(Collectors.toList()))
                .withFailMessage("Cashbacks should be equals")
                .isEqualTo(cardProductDtoList.stream().map(CardProductDto::getCashback).collect(Collectors.toList()));
        softAssertions.assertThat(actualList.stream().map(CardProductDto::getCoBrand).collect(Collectors.toList()))
                .withFailMessage("CoBrands should be equals")
                .isEqualTo(cardProductDtoList.stream().map(CardProductDto::getCoBrand).collect(Collectors.toList()));
        softAssertions.assertThat(actualList.stream().map(CardProductDto::getIsVirtual).collect(Collectors.toList()))
                .withFailMessage("Virtual status should be equals")
                .isEqualTo(cardProductDtoList.stream().map(CardProductDto::getIsVirtual).collect(Collectors.toList()));
        softAssertions.assertThat(actualList.stream().map(CardProductDto::getPremiumStatus).collect(Collectors.toList()))
                .withFailMessage("Premium statuses should be equals")
                .isEqualTo(cardProductDtoList.stream().map(CardProductDto::getPremiumStatus).collect(Collectors.toList()));
        softAssertions.assertThat(actualList.stream().map(CardProductDto::getServicePrice).collect(Collectors.toList()))
                .withFailMessage("Service prices should be equals")
                .isEqualTo(cardProductDtoList.stream().map(CardProductDto::getServicePrice).collect(Collectors.toList()));
        softAssertions.assertThat(actualList.stream().map(CardProductDto::getProductPrice).collect(Collectors.toList()))
                .withFailMessage("Product prices should be equals")
                .isEqualTo(cardProductDtoList.stream().map(CardProductDto::getProductPrice).collect(Collectors.toList()));
        softAssertions.assertThat(actualList.stream().map(CardProductDto::getCurrencyCode).collect(Collectors.toList()))
                .withFailMessage("Currency codes should be equals")
                .isEqualTo(cardProductDtoList.stream().map(CardProductDto::getCurrencyCode).collect(Collectors.toList()));
        softAssertions.assertThat(actualList.stream().map(CardProductDto::getIsActive).collect(Collectors.toList()))
                .withFailMessage("Active statuses should be equals")
                .isEqualTo(cardProductDtoList.stream().map(CardProductDto::getIsActive).collect(Collectors.toList()));
        softAssertions.assertThat(actualList.stream().map(CardProductDto::getCardDuration).collect(Collectors.toList()))
                .withFailMessage("Card durations should be equals")
                .isEqualTo(cardProductDtoList.stream().map(CardProductDto::getCardDuration).collect(Collectors.toList()));
        softAssertions.assertAll();
    }
}