package by.afinny.deposit.controller;

import by.afinny.deposit.dto.AccountWithCardInfoDto;
import by.afinny.deposit.dto.CardDebitLimitDto;
import by.afinny.deposit.dto.CardInfoDto;
import by.afinny.deposit.dto.CardNumberDto;
import by.afinny.deposit.dto.CardStatusDto;
import by.afinny.deposit.dto.CreatePaymentDepositDto;
import by.afinny.deposit.dto.NewPinCodeDebitCardDto;
import by.afinny.deposit.dto.ViewCardDto;
import by.afinny.deposit.service.AccountService;
import by.afinny.deposit.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("auth/deposit-cards")
@RequiredArgsConstructor
public class CardController {

    public static final String URL_CARDS = "/auth/deposit-cards/";
    public static final String URL_CARD_ID = "{cardId}";
    public static final String URL_CARD_INFORMATION = "{cardId}/information";
    public static final String URL_CARD_PIN_CODE = "/code";
    public static final String PARAM_CLIENT_ID = "clientId";
    public static final String URL_LIMIT = "/limit";
    public static final String URL_ACTIVE_CARDS = "/active-cards";


    private final AccountService accountService;
    private final CardService cardService;

    @GetMapping
    public ResponseEntity<List<AccountWithCardInfoDto>> getActiveProducts(@RequestParam UUID clientId) {
        List<AccountWithCardInfoDto> accountsWithCardsDto = accountService.getActiveAccountsWithCard(clientId);
        return ResponseEntity.ok(accountsWithCardsDto);
    }

    @PatchMapping("/active-cards")
    public ResponseEntity<Void> changeCardStatus(@RequestParam UUID clientId,
                                                 @RequestBody CardStatusDto cardStatus) {
        cardService.changeCardStatus(clientId, cardStatus);
        return ResponseEntity.ok().build();
    }

    @GetMapping("{cardId}")
    public ResponseEntity<ViewCardDto> getAccountByCardId(@RequestParam UUID clientId,
                                                          @PathVariable UUID cardId) {
        ViewCardDto viewCardDto = accountService.getViewCardByCardId(clientId, cardId);
        return ResponseEntity.ok(viewCardDto);
    }

    @PatchMapping("/limit")
    public ResponseEntity<Void> changeDebitCardLimit(@RequestParam UUID clientId,
                                                     @RequestBody CardDebitLimitDto cardDebitLimitDto) {
        cardService.changeDebitCardLimit(clientId, cardDebitLimitDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("{cardId}")
    public ResponseEntity<Void> deleteDebitCard(@RequestParam UUID clientId,
                                                @PathVariable UUID cardId) {
        cardService.deleteDebitCard(clientId, cardId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("{cardId}/information")
    public ResponseEntity<CardNumberDto> getCardNumberByCardId(@PathVariable UUID cardId) {
        CardNumberDto cardNumberDto = cardService.getCardNumberByCardId(cardId);
        return ResponseEntity.ok(cardNumberDto);
    }

    @PostMapping("/code")
    ResponseEntity<Void> changePinCodeDebitCard(@RequestParam UUID clientId,
                                                @RequestBody NewPinCodeDebitCardDto newPinCodeDebitCardDto) {
        cardService.changePinCodeDebitCard(clientId, newPinCodeDebitCardDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("{cardId}/info")
    public ResponseEntity<CardInfoDto> getCardInfo(@RequestParam UUID clientId,
                                                   @PathVariable UUID cardId) {
        CardInfoDto cardInfoDto = cardService.getCardInfo(clientId, cardId);
        return ResponseEntity.ok(cardInfoDto);
    }

    @PatchMapping("/{clientId}")
    ResponseEntity<Boolean> writeOffSum(@PathVariable UUID clientId,
                                        @RequestBody CreatePaymentDepositDto createPaymentDepositDto) {
        return ResponseEntity.ok(cardService.writeOffSum(clientId, createPaymentDepositDto));
    }
}