package by.afinny.deposit.service.impl;

import by.afinny.deposit.dto.AccountWithCardInfoDto;
import by.afinny.deposit.dto.ViewCardDto;
import by.afinny.deposit.dto.userservice.AccountDto;
import by.afinny.deposit.entity.Account;
import by.afinny.deposit.entity.Card;
import by.afinny.deposit.exception.EntityNotFoundException;
import by.afinny.deposit.mapper.AccountMapper;
import by.afinny.deposit.mapper.CardMapper;
import by.afinny.deposit.repository.AccountRepository;
import by.afinny.deposit.repository.CardRepository;
import by.afinny.deposit.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static by.afinny.deposit.entity.constant.CardStatus.BLOCKED;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;

    @Override
    public List<AccountDto> getActiveAccounts(UUID clientId) {
        log.info("getActiveAccounts() method invoke");
        List<Account> accounts = accountRepository.findByClientIdAndIsActiveTrue(clientId);
        return accountMapper.toAccountsDto(accounts);
    }

    @Override
    public List<AccountWithCardInfoDto> getActiveAccountsWithCard(UUID clientId) {
        log.info("getActiveAccountsWithCards() method invoke with clientId: {}", clientId);
        List<Account> accounts = accountRepository.findByClientIdAndIsActiveTrue(clientId);
        if (accounts.isEmpty()) {
            throw new EntityNotFoundException("There are no active accounts by id " + clientId);
        }
        List<Card> cards = accounts.stream().flatMap(a -> a.getCards().stream()).collect(Collectors.toList());
        return accountMapper.toAccountsWithCardsDto(cards);
    }

    @Override
    public ViewCardDto getViewCardByCardId(UUID clientId, UUID cardId) {
        log.info("getViewCardByCardId() method invoke with cardId: {}", cardId);
        Card card = cardRepository.findByAccountClientIdAndId(clientId, cardId).orElseThrow(
                () -> new EntityNotFoundException("card with card id " + cardId + " for client id " + clientId + " wasn't found"));
        return cardMapper.toViewCardDto(card);
    }
}