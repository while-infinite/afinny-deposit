package by.afinny.deposit.controller.userservice;

import by.afinny.deposit.dto.userservice.AccountDto;
import by.afinny.deposit.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping()
    public ResponseEntity<List<AccountDto>> getActiveAccounts(@RequestParam UUID clientId) {
        List<AccountDto> accountDto = accountService.getActiveAccounts(clientId);
        return ResponseEntity.ok(accountDto);
    }
}
