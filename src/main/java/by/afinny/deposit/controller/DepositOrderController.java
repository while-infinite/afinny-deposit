package by.afinny.deposit.controller;

import by.afinny.deposit.dto.RequestNewDepositDto;
import by.afinny.deposit.service.DepositService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("auth/deposit-orders")
public class DepositOrderController {

    public static final String DEPOSIT_ORDER_URL = "/auth/deposit-orders";
    public static final String NEW_DEPOSIT_URL = "/new";
    private final DepositService depositService;

    @PostMapping("new")
    ResponseEntity<Void> createNewDeposit(@RequestParam UUID clientId,
                                          @RequestBody RequestNewDepositDto requestNewDepositDto) {
        depositService.createNewDeposit(clientId, requestNewDepositDto);
        return ResponseEntity.ok().build();
    }
}
