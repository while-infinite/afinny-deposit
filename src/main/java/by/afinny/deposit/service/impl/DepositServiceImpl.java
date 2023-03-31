package by.afinny.deposit.service.impl;

import by.afinny.deposit.dto.DepositDto;
import by.afinny.deposit.dto.ActiveDepositDto;
import by.afinny.deposit.dto.RefillDebitCardDto;
import by.afinny.deposit.dto.RequestNewDepositDto;
import by.afinny.deposit.entity.Account;
import by.afinny.deposit.entity.Agreement;
import by.afinny.deposit.entity.Card;
import by.afinny.deposit.entity.Operation;
import by.afinny.deposit.entity.Product;
import by.afinny.deposit.exception.EntityNotFoundException;
import by.afinny.deposit.mapper.DepositMapper;
import by.afinny.deposit.repository.AccountRepository;
import by.afinny.deposit.repository.AgreementRepository;
import by.afinny.deposit.repository.CardRepository;
import by.afinny.deposit.repository.OperationRepository;
import by.afinny.deposit.repository.ProductRepository;
import by.afinny.deposit.service.DepositService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DepositServiceImpl implements DepositService {

    private final ApplicationEventPublisher eventPublisher;
    private final AgreementRepository agreementRepository;
    private final DepositMapper depositMapper;
    private final CardRepository cardRepository;
    private final ProductRepository productRepository;
    private final AccountRepository accountRepository;
    private final OperationRepository operationRepository;

    @Override
    public void createNewDeposit(UUID clientId, RequestNewDepositDto requestNewDepositDto) {
        log.info("createNewDeposit() method invoke");
        verifyRequestNewDepositOrThrow(clientId, requestNewDepositDto);
        sendToKafka(requestNewDepositDto);
    }

    @Override
    @Transactional
    public void saveAgreement(Agreement agreement) {
        log.info("saveAgreement() method invoke");
        agreementRepository.save(agreement);
    }

    @Override
    public DepositDto getDeposit(UUID clientId, UUID agreementId, UUID cardId) {
        log.info("getDeposit() method invoke");
        Agreement agreement = getAgreement(clientId, agreementId);
        Product product = agreement.getProduct();
        Card card = getCard(cardId);
        return depositMapper.toDepositDto(agreement, product, card);
    }

    @Override
    public List<ActiveDepositDto> getActiveDeposits(UUID clientId) {
        log.info("getActiveDeposits() method invoke with clientId: {}", clientId);
        List<Agreement> agreements = agreementRepository.findByAccountClientIdAndIsActiveTrue(clientId);
        return depositMapper.toActiveDepositsDto(agreements);
    }

    @Override
    @Transactional
    public void refillUserDebitCard(UUID clientId, RefillDebitCardDto refillDebitCardDto) {
        log.info("refillUserDebitCard() method invoke");
        String accountNumber = refillDebitCardDto.getAccountNumber();
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new EntityNotFoundException("account with id " + accountNumber + "for client id " + clientId + " not found"));
        Operation operation = depositMapper.refillDebitCardDtoToOperation(refillDebitCardDto, account, LocalDateTime.now());
        if(account.getOperations() != null)
            account.getOperations().add(operation);
        else {
            List<Operation> operations = new ArrayList<>();
            operations.add(operation);
            account.setOperations(operations);
        }
        operationRepository.save(operation);
    }

    private Agreement getAgreement(UUID clientId, UUID agreementId) {
        return agreementRepository.findByAccountClientIdAndId(clientId, agreementId).orElseThrow(
                () -> new EntityNotFoundException("agreement with id " + agreementId + "for client id " + clientId + " not found"));
    }

    private Card getCard(UUID cardId) {
        return cardRepository.findById(cardId).orElseThrow(
                () -> new EntityNotFoundException("card with id " + cardId + " not found"));
    }

    private void sendToKafka(RequestNewDepositDto requestNewDepositDto) {
        log.info("sendToKafka() method invoke");
        eventPublisher.publishEvent(requestNewDepositDto);
    }

    private void verifyRequestNewDepositOrThrow(UUID clientId, RequestNewDepositDto requestNewDepositDto) {
        String cardNumber = requestNewDepositDto.getCardNumber();
        cardRepository.findByAccountClientIdAndCardNumber(clientId, cardNumber).orElseThrow(
                () -> new EntityNotFoundException("card with number " + cardNumber + " for client id " + clientId + " not found")
        );
        Integer productId = requestNewDepositDto.getProductId();
        productRepository.findById(productId).orElseThrow(
                () -> new EntityNotFoundException("product with id " + productId + " not found")
        );
    }

}
