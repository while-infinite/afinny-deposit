package by.afinny.deposit.mapper;

import by.afinny.deposit.dto.ClientDto;
import by.afinny.deposit.dto.userservice.ClientByPhoneDto;
import by.afinny.deposit.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ClientMapper {

    @Mapping(source = "account.accountNumber", target = "accountNumber")
    @Mapping(source = "clientByPhoneDto.clientId", target = "clientId")
    ClientDto toClientDto(ClientByPhoneDto clientByPhoneDto, Account account);
}
