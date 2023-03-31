package by.afinny.deposit.service;

import by.afinny.deposit.dto.DepositDto;
import by.afinny.deposit.dto.ActiveDepositDto;
import by.afinny.deposit.dto.RefillDebitCardDto;
import by.afinny.deposit.dto.RequestNewDepositDto;
import by.afinny.deposit.entity.Agreement;

import java.util.UUID;
import java.util.List;

public interface DepositService {

    void createNewDeposit(UUID clientId, RequestNewDepositDto requestNewDepositDto);

    void saveAgreement(Agreement agreement);

    DepositDto getDeposit(UUID clientId, UUID agreementId, UUID cardId);

    List<ActiveDepositDto> getActiveDeposits(UUID clientId);

    void refillUserDebitCard(UUID clientId, RefillDebitCardDto refillDebitCardDto);
}
