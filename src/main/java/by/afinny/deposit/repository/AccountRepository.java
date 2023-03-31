package by.afinny.deposit.repository;

import by.afinny.deposit.entity.Account;
import by.afinny.deposit.entity.constant.CurrencyCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    List<Account> findByClientIdAndIsActiveTrue(UUID clientId);

    Optional<Account> findByAccountNumber(String accountNumber);

    Optional<Account> getAccountsByClientIdAndCurrencyCode(UUID clientId, CurrencyCode currencyCode);

}