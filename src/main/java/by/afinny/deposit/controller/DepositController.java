package by.afinny.deposit.controller;

import by.afinny.deposit.dto.ActiveDepositDto;
import by.afinny.deposit.dto.AutoRenewalDto;
import by.afinny.deposit.dto.DepositDto;
import by.afinny.deposit.dto.RefillDebitCardDto;
import by.afinny.deposit.dto.WithdrawDepositDto;
import by.afinny.deposit.service.AgreementService;
import by.afinny.deposit.service.DepositService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("auth/deposits")
@Slf4j
public class DepositController {

    public static final String URL_DEPOSITS = "/auth/deposits/";
    public static final String URL_DEPOSITS_AUTO_RENEWAL = "{agreementId}/auto-renewal";

    private final DepositService depositService;
    private final AgreementService agreementService;

    @PatchMapping("{agreementId}/revocation")
    @ResponseStatus(HttpStatus.OK)
    public void earlyWithdrawalDeposit(@RequestParam UUID clientId,
                                                       @PathVariable UUID agreementId,
                                                       @RequestBody WithdrawDepositDto withdrawDepositDto) {
        agreementService.earlyWithdrawalDeposit(clientId, agreementId, withdrawDepositDto);
    }

    @GetMapping("{agreementId}")
    public ResponseEntity<DepositDto> getDeposit(@RequestParam UUID clientId,
                                                 @PathVariable UUID agreementId,
                                                 @RequestParam UUID cardId) {
        return ResponseEntity.ok(depositService.getDeposit(clientId, agreementId, cardId));
    }

    @GetMapping
    public ResponseEntity<List<ActiveDepositDto>> getActiveDeposits(@RequestParam UUID clientId) {
        List<ActiveDepositDto> agreementsDto = depositService.getActiveDeposits(clientId);
        return ResponseEntity.ok(agreementsDto);
    }

    @PatchMapping("{agreementId}/auto-renewal")
    @ResponseStatus(HttpStatus.OK)
    public void updateAutoRenewal(@RequestParam UUID clientId,
                                                  @PathVariable("agreementId") UUID agreementId,
                                                  @RequestBody AutoRenewalDto autoRenewalDto) {
        agreementService.updateAutoRenewal(clientId, agreementId, autoRenewalDto);
    }

    @PostMapping("/refill")
    @ResponseStatus(HttpStatus.OK)
    public void refillUserDebitCard(@RequestParam UUID clientId,
                                                    @RequestBody RefillDebitCardDto refillDebitCardDto){
        depositService.refillUserDebitCard(clientId, refillDebitCardDto);
    }
}
