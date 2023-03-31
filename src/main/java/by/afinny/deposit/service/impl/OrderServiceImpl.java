package by.afinny.deposit.service.impl;

import by.afinny.deposit.dto.RequestNewCardDto;
import by.afinny.deposit.dto.kafka.ConsumerNewCardEvent;
import by.afinny.deposit.dto.kafka.ProducerNewCardEvent;
import by.afinny.deposit.entity.Account;
import by.afinny.deposit.entity.Card;
import by.afinny.deposit.mapper.CardMapper;
import by.afinny.deposit.repository.AccountRepository;
import by.afinny.deposit.repository.CardRepository;
import by.afinny.deposit.repository.ProductRepository;
import by.afinny.deposit.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import by.afinny.deposit.exception.EntityNotFoundException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final ApplicationEventPublisher eventPublisher;
    private final CardMapper cardMapper;
    private final CardRepository cardRepository;
    private final AccountRepository accountRepository;
    private final ProductRepository productRepository;

    @Override
    public void orderNewCard(UUID clientId, RequestNewCardDto requestNewCardDto) {
        log.info("orderNewCard() method invoke with clientId: {} requestNewCardDto: {}", clientId, requestNewCardDto);

        productRepository.findProductById(requestNewCardDto.getProductId()).orElseThrow(
                () -> new EntityNotFoundException("product with id " + requestNewCardDto.getProductId() + " wasn't found"));
        sendToKafka(clientId, requestNewCardDto);
    }

    @Transactional
    public void createNewCard(ConsumerNewCardEvent consumerNewCardEvent) {
        log.info("createNewCard() method invoke with event: {}", consumerNewCardEvent);
        Card card = cardMapper.toCard(consumerNewCardEvent);

        Account account = getAccount(consumerNewCardEvent);
        card.setAccount(account);

        cardRepository.save(card);
    }

    private Account getAccount(ConsumerNewCardEvent consumerNewCardEvent) {
        return accountRepository.findByAccountNumber(consumerNewCardEvent.getAccountNumber())
                .orElseThrow(
                        () -> new EntityNotFoundException("Account with number: " + consumerNewCardEvent.getCardNumber() +
                                " not found"));
    }

    private void sendToKafka(UUID clientId, RequestNewCardDto requestNewCardDto) {
        ProducerNewCardEvent event = cardMapper.toProducerNewCardEvent(clientId, requestNewCardDto);
        log.info("Publishing event...");
        eventPublisher.publishEvent(event);
    }
}
