package by.afinny.deposit.service.impl;

import by.afinny.deposit.dto.ClientDto;
import by.afinny.deposit.dto.userservice.ClientByPhoneDto;
import by.afinny.deposit.entity.Account;
import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.mapper.ClientMapper;
import by.afinny.deposit.openfeign.userservice.UserClient;
import by.afinny.deposit.repository.AccountRepository;
import by.afinny.deposit.service.ClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import by.afinny.deposit.exception.EntityNotFoundException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientServiceImpl implements ClientService {

    private final UserClient userClient;

    private final AccountRepository accountRepository;

    private final ClientMapper clientMapper;

    @Override
    public ClientDto getClientByPhoneNumber(UUID clientId, CurrencyCode currencyCode, String mobilePhone) {
        log.info("getClientByPhoneNumber() method invoke");
        ClientByPhoneDto clientByPhoneDto = userClient.getClientByPhone(mobilePhone).getBody();
        if(clientByPhoneDto==null||!clientByPhoneDto.getClientId().equals(clientId)) {
            throw new EntityNotFoundException("clientId found by phone differs from auth client id = " + clientId);
        }
        Account account = accountRepository.getAccountsByClientIdAndCurrencyCode(clientByPhoneDto.getClientId(), currencyCode).orElseThrow(
                () -> new EntityNotFoundException("no currencyCode " + currencyCode + " by this was found"));
        return clientMapper.toClientDto(clientByPhoneDto, account);
    }
}