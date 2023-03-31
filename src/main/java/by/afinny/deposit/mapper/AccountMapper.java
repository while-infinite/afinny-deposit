package by.afinny.deposit.mapper;

import by.afinny.deposit.dto.AccountNumberDto;
import by.afinny.deposit.dto.AccountWithCardInfoDto;
import by.afinny.deposit.dto.ViewCardDto;
import by.afinny.deposit.dto.userservice.AccountDto;
import by.afinny.deposit.entity.Account;
import by.afinny.deposit.entity.Card;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(uses = CardMapper.class)
public interface AccountMapper {

    List<AccountDto> toAccountsDto(List<Account> accounts);

    @Mapping(source = "cardBalance", target = "balance")
    List<AccountWithCardInfoDto> toAccountsWithCardsDto(List<Card> cards);

    @Mapping(source = "id", target = "cardId")
    @Mapping(target = "currencyCode", expression = "java(card.getAccount().getCurrencyCode())")
    @Mapping(target = "cardBalance", expression = "java(card.getBalance())")
    @Mapping(target = "cardName", expression = "java(card.getCardProduct().getCardName())")
    @Mapping(target = "paymentSystem", expression = "java(card.getCardProduct().getPaymentSystem())")
    AccountWithCardInfoDto cardToAccountsWithCardsDto(Card card);

}