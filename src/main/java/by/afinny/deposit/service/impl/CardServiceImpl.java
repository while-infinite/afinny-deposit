package by.afinny.deposit.service.impl;

import by.afinny.deposit.dto.CardDebitLimitDto;
import by.afinny.deposit.dto.CardInfoDto;
import by.afinny.deposit.dto.CardNumberDto;
import by.afinny.deposit.dto.CardStatusDto;
import by.afinny.deposit.dto.CreatePaymentDepositDto;
import by.afinny.deposit.dto.NewPinCodeDebitCardDto;
import by.afinny.deposit.dto.kafka.CardEvent;
import by.afinny.deposit.entity.Card;
import by.afinny.deposit.entity.constant.CardStatus;
import by.afinny.deposit.exception.CardStatusesAreEqualsException;
import by.afinny.deposit.exception.EntityNotFoundException;
import by.afinny.deposit.mapper.CardMapper;
import by.afinny.deposit.repository.CardRepository;
import by.afinny.deposit.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final CardMapper cardMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void changeCardStatus(UUID clientId, CardStatusDto cardStatusDto) {
        log.info("changeCardStatus() method invoke");
        CardStatus newCardStatus = cardStatusDto.getCardStatus();
        Card foundCard = getCardByClientIdAndId(clientId, cardStatusDto.getCardNumber());
        UUID cardId = foundCard.getId();
        checkStatusesEqual(foundCard.getStatus(), newCardStatus);
        foundCard.setStatus(newCardStatus);
        sendToKafka(cardId, newCardStatus);
        cardRepository.save(foundCard);
    }

    @Override
    public void modifyCardStatus(UUID cardId, CardStatus newCardStatus) {
        log.info("modifyCardStatus() method invoked");
        Card foundCard = getCardById(cardId);
        CardStatus foundCardStatus = foundCard.getStatus();
        checkStatusesEqual(foundCardStatus, newCardStatus);
        log.info("Updating card status from " + foundCardStatus + " to " + newCardStatus);
        foundCard.setStatus(newCardStatus);
        cardRepository.save(foundCard);
    }

    @Override
    public void changeDebitCardLimit(UUID clientId, CardDebitLimitDto newCardDebitLimitDto) {
        log.info("changeDebitCardLimit() method invoke");
        Card debitCardLimit = cardRepository.findByAccountClientIdAndCardNumber(clientId, newCardDebitLimitDto.getCardNumber())
                .orElseThrow(() -> new EntityNotFoundException("no card with the specified number " +
                        newCardDebitLimitDto.getCardNumber() + " for client id " + clientId + " was found"));
        debitCardLimit.setTransactionLimit(newCardDebitLimitDto.getTransactionLimit());
        cardRepository.save(debitCardLimit);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteDebitCard(UUID clientId, UUID cardId) {
        log.info("deleteDebitCard() method invoked");
        if (cardRepository.findByAccountClientIdAndId(clientId, cardId).isPresent()) {
            cardRepository.deleteById(cardId);
        } else {
            throw new EntityNotFoundException("debit card with id " + cardId + " for client id " + clientId + " wasn't found");
        }
    }

    @Override
    public CardNumberDto getCardNumberByCardId(UUID cardId) {
        log.info("getCardNumberByCardId() method invoke with cardId: {}", cardId);
        Card card = getCardById(cardId);
        return cardMapper.toCardNumberDto(card.getCardNumber());
    }

    @Override
    public void changePinCodeDebitCard(UUID clientId, NewPinCodeDebitCardDto newPinCodeDebitCardDto) {
        log.info("changePinCodeDebitCard() method invoke");
        cardRepository.findByAccountClientIdAndCardNumber(clientId, newPinCodeDebitCardDto.getCardNumber()).orElseThrow(
                () -> new EntityNotFoundException("no card with the specified number " +
                       newPinCodeDebitCardDto.getCardNumber() + " for client id " + clientId + " was found"));
        sendToKafka(newPinCodeDebitCardDto);
    }

    @Override
    public CardInfoDto getCardInfo(UUID clientId, UUID cardId) {
        log.info("getCardInfo() method invoke");

        Card card = cardRepository.findByAccountClientIdAndIdAndStatusNot(clientId, cardId, CardStatus.CLOSED).orElseThrow(
                () -> new EntityNotFoundException("debit card with card id " + cardId + " for client id " + clientId + " wasn't found")
        );
        return cardMapper.toCardInfoDto(card);
    }

    @Override
    @Transactional
    public Boolean writeOffSum(UUID clientId, CreatePaymentDepositDto createPaymentDepositDto) {
        log.info("writeOffSum() method invoke with cardId: {}", createPaymentDepositDto.getRemitterCardNumber());
        Card card = cardRepository.findByCardNumber(createPaymentDepositDto.getRemitterCardNumber())
                .orElseThrow(() -> new EntityNotFoundException("Card with card number " +
                        createPaymentDepositDto.getRemitterCardNumber() + " wasn't found"));


        BigDecimal sum = card.getBalance().subtract(createPaymentDepositDto.getSum());
        if (sum.compareTo(BigDecimal.valueOf(0.0)) != -1) {
            card.setBalance(sum);
            cardRepository.saveAndFlush(card);
            log.info("Card balance " + card.getCardNumber() + " updated");
            return true;
        } else {
            throw new RuntimeException("Insufficient funds in the brokerage account " + card.getCardNumber());
        }
    }

    private Card getCardById(UUID cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new EntityNotFoundException("Card with card number " + cardId + " wasn't found"));
    }

    private Card getCardByClientIdAndId(UUID clientId, String cardNumber) {
        return cardRepository.findByAccountClientIdAndCardNumber(clientId, cardNumber)
                .orElseThrow(() -> new EntityNotFoundException("Card with card number " + cardNumber + " for client id " + clientId + " wasn't found"));
    }

    private void sendToKafka(UUID cardId, CardStatus newCardStatus) {
        CardEvent event = cardMapper.toCardEvent(cardId, newCardStatus);
        log.info("Publishing event...");
        eventPublisher.publishEvent(event);
    }

    private void sendToKafka(NewPinCodeDebitCardDto newPinCodeDebitCardDto) {
        log.info("Publishing event...");
        eventPublisher.publishEvent(newPinCodeDebitCardDto);
    }

    private void checkStatusesEqual(CardStatus oldStatus, CardStatus newStatus) {
        if (oldStatus.equals(newStatus)) {
            log.info("Card statuses are the same");
            throw new CardStatusesAreEqualsException(
                    Integer.toString(HttpStatus.BAD_REQUEST.value()),
                    "The same card status already exists!");
        }
    }
}