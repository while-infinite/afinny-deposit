package by.afinny.deposit.repository;

import by.afinny.deposit.entity.Card;
import by.afinny.deposit.entity.constant.CardStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {

    Optional<Card> findByCardNumber(String cardNumber);

    Optional<Card> findByAccountClientIdAndCardNumber(UUID clientId, String cardNumber);

    Optional<Card> findByAccountClientIdAndId(UUID clientId, UUID cardId);

    void deleteById(UUID cardId);

    Optional<Card> findByIdAndStatusNot(UUID cardId, CardStatus excludingStatus);

    Optional<Card> findByAccountClientIdAndIdAndStatusNot(UUID clientId, UUID cardId, CardStatus excludingStatus);
}
