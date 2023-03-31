package by.afinny.deposit.service.impl;

import by.afinny.deposit.dto.AutoRenewalDto;
import by.afinny.deposit.dto.WithdrawDepositDto;
import by.afinny.deposit.dto.kafka.AutoRenewalEvent;
import by.afinny.deposit.dto.kafka.ConsumerWithdrawEvent;
import by.afinny.deposit.dto.kafka.ProducerWithdrawEvent;
import by.afinny.deposit.entity.Account;
import by.afinny.deposit.entity.Agreement;
import by.afinny.deposit.entity.Card;
import by.afinny.deposit.entity.Operation;
import by.afinny.deposit.entity.OperationType;
import by.afinny.deposit.exception.EntityNotFoundException;
import by.afinny.deposit.exception.handler.CardExpiredException;
import by.afinny.deposit.mapper.OperationMapper;
import by.afinny.deposit.repository.AccountRepository;
import by.afinny.deposit.repository.AgreementRepository;
import by.afinny.deposit.repository.CardRepository;
import by.afinny.deposit.repository.OperationRepository;
import by.afinny.deposit.service.AgreementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgreementServiceImpl implements AgreementService {

    private final AgreementRepository agreementRepository;
    private final OperationRepository operationRepository;
    private final AccountRepository accountRepository;
    private final OperationMapper operationMapper;
    private final ApplicationEventPublisher eventPublisher;
    private final CardRepository cardRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void earlyWithdrawalDeposit(UUID clientId, UUID agreementId, WithdrawDepositDto withdrawDepositDto) {
        log.info("earlyWithdrawalDeposit() invoked");
        Card card = cardRepository.findByCardNumber(withdrawDepositDto.getCardNumber())
                .orElseThrow(() -> new EntityNotFoundException("card with number " + withdrawDepositDto.getCardNumber() + " wasn't found"));

        if(card.getExpirationDate().isBefore(LocalDate.now()))
            throw new CardExpiredException("The card expired");

        BigDecimal currentBalance = agreementRepository.findById(agreementId)
                .orElseThrow(() -> new EntityNotFoundException("agreement with id " + agreementId + " wasn't found"))
                .getCurrentBalance();
        card.setBalance(card.getBalance().add(currentBalance));
        cardRepository.save(card);
        Account account = card.getAccount();
        sendToKafka(agreementId, account.getAccountNumber());

        //stub communication with ABS
        consumeFromKafka(clientId, agreementId, account.getAccountNumber());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void modifyAgreementAndCreateOperation(ConsumerWithdrawEvent consumerWithdrawEvent) {
        log.info("modifyAgreementAndInsertOperation() invoked");

        Account account = accountRepository.findByAccountNumber(consumerWithdrawEvent.getAccountNumber())
                .orElseThrow(() -> new EntityNotFoundException("account with number " + consumerWithdrawEvent.getAccountNumber() + " wasn't found"));
        account.setCurrentBalance(account.getCurrentBalance().add(consumerWithdrawEvent.getSum()));

        modifyAgreement(consumerWithdrawEvent);

        Operation operation = createOperation(consumerWithdrawEvent, account);
        operationRepository.save(operation);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Override
    public void updateAutoRenewal(UUID clientId, UUID agreementId, AutoRenewalDto autoRenewalDto) {
        log.info("updateAutoRenewal() method invoked");
        Agreement foundAgreement = getAgreementByIdWhereIsActiveTrue(clientId, agreementId);
        foundAgreement.setAutoRenewal(autoRenewalDto.getAutoRenewal());
        sendToKafka(agreementId, autoRenewalDto.getAutoRenewal());
        agreementRepository.save(foundAgreement);
    }

    private void sendToKafka(UUID agreementId, boolean autoRenewal) {
        AutoRenewalEvent event = createAutoRenewalEvent(agreementId, autoRenewal);
        log.info("Publishing event...");
        eventPublisher.publishEvent(event);
    }

    private void sendToKafka(UUID agreementId, String accountNumber) {
        ProducerWithdrawEvent event = new ProducerWithdrawEvent();
        event.setAgreementId(agreementId);
        event.setAccountNumber(accountNumber);
        log.info("Publishing event...");
        eventPublisher.publishEvent(event);
    }

    private void modifyAgreement(ConsumerWithdrawEvent consumerWithdrawEvent) {

        Agreement agreement = agreementRepository.findById(consumerWithdrawEvent.getAgreementId()).orElseThrow(
                () -> new EntityNotFoundException("agreement with id " + consumerWithdrawEvent.getAgreementId() + " wasn't found"));

        agreement.setIsActive(consumerWithdrawEvent.getIsActive());
        agreement.setCurrentBalance(consumerWithdrawEvent.getCurrentBalance());
        agreement.setEndDate(LocalDateTime.now().plusYears(1));
        agreementRepository.save(agreement);
    }

    private Operation createOperation(ConsumerWithdrawEvent consumerWithdrawEvent, Account account) {
        Operation operation = operationMapper.consumerWithdrawEventToOperation(consumerWithdrawEvent);
        operation.setAccount(account);
        return operation;
    }

    private Agreement getAgreementByIdWhereIsActiveTrue(UUID clientId, UUID agreementId) {
        return agreementRepository.findAgreementByAccountClientIdAndIdAndIsActiveTrue(clientId, agreementId)
                .orElseThrow(() -> new EntityNotFoundException("Agreement with id " + agreementId + "for client id " + clientId + " wasn't found"));
    }

    private AutoRenewalEvent createAutoRenewalEvent(UUID agreementId, boolean autoRenewal) {
        return AutoRenewalEvent.builder()
                .agreementId(agreementId)
                .autoRenewal(autoRenewal)
                .build();
    }

    /**
     * stub communication with ABS
     */
    private void consumeFromKafka(UUID clientId, UUID agreementId, String accountNumber) {
        ConsumerWithdrawEvent mockConsumerWithdrawEvent = getMockEvent(clientId, agreementId, accountNumber);
        modifyAgreementAndCreateOperation(mockConsumerWithdrawEvent);
    }


    private ConsumerWithdrawEvent getMockEvent(UUID clientId, UUID agreementId, String accountNumber) {

        Agreement agreement = agreementRepository.findByAccountClientIdAndId(clientId, agreementId).orElseThrow(
                () -> new EntityNotFoundException("agreement with id " + agreementId + "for client id " + clientId + " wasn't found"));

        ConsumerWithdrawEvent consumerWithdrawEvent = ConsumerWithdrawEvent.builder()
                .agreementId(agreementId)
                .accountNumber(accountNumber)
                .isActive(Boolean.FALSE)
                .currentBalance(BigDecimal.valueOf(0))
                .completedAt(LocalDateTime.now().minusMinutes(10).toString())
                .sum(agreement.getCurrentBalance().multiply(agreement.getProduct().getInterestRateEarly()))
                .currencyCode(agreement.getProduct().getCurrencyCode())
                .type(OperationType.builder()
                        .id(1)
                        .type("REPLENISHMENT")
                        .debit(true).build())
                .build();
        return consumerWithdrawEvent;
    }
}