package by.afinny.deposit.controller;

import by.afinny.deposit.dto.ClientDto;
import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("auth/accounts")
@RequiredArgsConstructor
public class ClientByPhoneController {

    private final ClientService clientService;

    @GetMapping
    public ResponseEntity<ClientDto> getClientByPhone(@RequestParam UUID clientId,
                                                      @RequestParam(name = "mobilePhone") String mobilePhone,
                                                      @RequestParam(name = "currency_code") CurrencyCode currencyCode) {
        ClientDto clientDto = clientService.getClientByPhoneNumber(clientId, currencyCode, mobilePhone);
        return ResponseEntity.ok(clientDto);
    }
}
