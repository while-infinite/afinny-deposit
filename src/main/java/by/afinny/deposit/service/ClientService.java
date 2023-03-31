package by.afinny.deposit.service;

import by.afinny.deposit.dto.ClientDto;
import by.afinny.deposit.entity.constant.CurrencyCode;
import java.util.UUID;

public interface ClientService {

    ClientDto getClientByPhoneNumber(UUID clientId, CurrencyCode currencyCode, String mobilePhone);
}
