package by.afinny.deposit.service;

import by.afinny.deposit.dto.RequestNewCardDto;
import by.afinny.deposit.dto.kafka.ConsumerNewCardEvent;

import java.util.UUID;

public interface OrderService {

    void orderNewCard(UUID clientId, RequestNewCardDto requestNewCardDto);

    void createNewCard(ConsumerNewCardEvent consumerNewCardEvent);
}
