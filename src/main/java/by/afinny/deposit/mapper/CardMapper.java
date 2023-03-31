package by.afinny.deposit.mapper;

import by.afinny.deposit.dto.CardDto;
import by.afinny.deposit.dto.CardInfoDto;
import by.afinny.deposit.dto.CardNumberDto;
import by.afinny.deposit.dto.RequestNewCardDto;
import by.afinny.deposit.dto.ViewCardDto;
import by.afinny.deposit.dto.kafka.CardEvent;
import by.afinny.deposit.dto.kafka.ConsumerNewCardEvent;
import by.afinny.deposit.dto.kafka.ProducerNewCardEvent;
import by.afinny.deposit.entity.Card;
import by.afinny.deposit.entity.constant.CardStatus;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.UUID;

@Mapper
public interface CardMapper {

    List<CardDto> toCardsDto(List<Card> card);

    @Mapping(source = "cardId", target = "cardId")
    @Mapping(source = "cardStatus", target = "cardStatus")
    CardEvent toCardEvent(UUID cardId, CardStatus cardStatus);

    @Mapping(target = "isDefault", constant = "false")
    @Mapping(source = "cardProductId", target = "cardProduct.id")
    Card toCard(ConsumerNewCardEvent consumerNewCardEvent);

    ProducerNewCardEvent toProducerNewCardEvent(UUID clientId, RequestNewCardDto requestNewCardDto);

    @Mapping(source = "id", target = "cardId")
    @Mapping(source = "cardProduct.id", target = "cardProductId")
    @Mapping(source = "cardProduct.cardName", target = "cardName")
    @Mapping(source = "cardProduct.paymentSystem", target = "paymentSystem")
    CardDto cardToCardsDto(Card card);

    CardNumberDto toCardNumberDto(String cardNumber);

    CardInfoDto toCardInfoDto(Card card);

    @Mapping(source = "id", target = "cardId")
    @Mapping(target = "currencyCode", expression = "java(card.getAccount().getCurrencyCode())")
    @Mapping(target = "cardBalance", expression = "java(card.getBalance())")
    @Mapping(target = "cardName", expression = "java(card.getCardProduct().getCardName())")
    @Mapping(target = "paymentSystem", expression = "java(card.getCardProduct().getPaymentSystem())")
    @Mapping(target = "accountId", expression = "java(card.getAccount().getId())")
    @Mapping(target = "accountNumber", expression = "java(card.getAccount().getAccountNumber())")
    ViewCardDto toViewCardDto(Card card);
}