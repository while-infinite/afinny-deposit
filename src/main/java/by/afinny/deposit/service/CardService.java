package by.afinny.deposit.service;

import by.afinny.deposit.dto.CardDebitLimitDto;
import by.afinny.deposit.dto.CardInfoDto;
import by.afinny.deposit.dto.CardNumberDto;
import by.afinny.deposit.dto.CardStatusDto;
import by.afinny.deposit.dto.CreatePaymentDepositDto;
import by.afinny.deposit.dto.NewPinCodeDebitCardDto;
import by.afinny.deposit.entity.constant.CardStatus;

import java.util.UUID;

public interface CardService {

    void changeCardStatus(UUID clientId, CardStatusDto newCardStatus);

    void modifyCardStatus(UUID cardId, CardStatus newCardStatus);

    void changeDebitCardLimit(UUID clientId, CardDebitLimitDto cardDebitLimitDto);

    void deleteDebitCard(UUID clientId, UUID cardId);

    CardNumberDto getCardNumberByCardId(UUID cardId);

    void changePinCodeDebitCard(UUID clientId, NewPinCodeDebitCardDto newPinCodeDebitCardDto);

    CardInfoDto getCardInfo(UUID clientId, UUID cardId);

    Boolean writeOffSum(UUID clientId, CreatePaymentDepositDto createPaymentDepositDto);
}