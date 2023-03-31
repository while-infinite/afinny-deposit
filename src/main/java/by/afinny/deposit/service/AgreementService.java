package by.afinny.deposit.service;

import by.afinny.deposit.dto.AutoRenewalDto;
import by.afinny.deposit.dto.WithdrawDepositDto;
import by.afinny.deposit.dto.kafka.ConsumerWithdrawEvent;

import java.util.UUID;

public interface AgreementService {

    void earlyWithdrawalDeposit(UUID clientId, UUID agreementId, WithdrawDepositDto withdrawDepositDto);

    void modifyAgreementAndCreateOperation(ConsumerWithdrawEvent consumerWithdrawEvent);

    void updateAutoRenewal(UUID clientId, UUID agreementId, AutoRenewalDto autoRenewalDto);
}
