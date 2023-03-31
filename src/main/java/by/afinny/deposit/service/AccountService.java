package by.afinny.deposit.service;

import by.afinny.deposit.dto.AccountWithCardInfoDto;
import by.afinny.deposit.dto.ViewCardDto;
import by.afinny.deposit.dto.userservice.AccountDto;

import java.util.List;
import java.util.UUID;

public interface AccountService {

    List<AccountDto> getActiveAccounts(UUID clientId);

    List<AccountWithCardInfoDto> getActiveAccountsWithCard(UUID clientId);

    ViewCardDto getViewCardByCardId(UUID clientId, UUID cardId);
}