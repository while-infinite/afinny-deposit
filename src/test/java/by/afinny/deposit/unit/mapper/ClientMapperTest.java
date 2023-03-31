package by.afinny.deposit.unit.mapper;

import by.afinny.deposit.dto.ClientDto;
import by.afinny.deposit.dto.userservice.ClientByPhoneDto;
import by.afinny.deposit.entity.Account;
import by.afinny.deposit.entity.constant.CurrencyCode;
import by.afinny.deposit.mapper.ClientMapperImpl;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
@ActiveProfiles("test")
public class ClientMapperTest {

    @InjectMocks
    private ClientMapperImpl clientMapper;

    private final UUID CLIENT_ID = UUID.randomUUID();
    private final String FIRST_NAME = "Ivan";
    private final String LAST_NAME = "Ivanov";
    private final String MIDDLE_NAME = "Ivanovich";

    private Account account;
    private ClientDto clientDto;
    private ClientByPhoneDto clientByPhoneDto;

    @BeforeEach
    void setUp() {
        account = Account.builder()
                .id(UUID.randomUUID())
                .accountNumber("accountNumber")
                .clientId(CLIENT_ID)
                .currencyCode(CurrencyCode.RUB)
                .closeDate(LocalDate.now())
                .openDate(LocalDate.now().minusYears(2))
                .currentBalance(BigDecimal.ONE).build();

        clientByPhoneDto = ClientByPhoneDto.builder()
                .clientId(CLIENT_ID)
                .firstName(FIRST_NAME)
                .middleName(MIDDLE_NAME)
                .lastName(LAST_NAME).build();
    }

    @Test
    @DisplayName("Check fields of ClientDto, Account and ClientByPhoneDto are equals")
    void toClientDto_shouldReturnClientDto() {
        //ACT
        clientDto = clientMapper.toClientDto(clientByPhoneDto, account);
        //VERIFY
        verifyClientDto(clientDto, account, clientByPhoneDto);
    }

    private void verifyClientDto(ClientDto clientDto, Account account, ClientByPhoneDto clientByPhoneDto) {
        SoftAssertions.assertSoftly(softAssertions -> {
            softAssertions.assertThat(clientDto.getClientId()).isEqualTo(account.getClientId());
            softAssertions.assertThat(clientDto.getClientId()).isEqualTo(clientByPhoneDto.getClientId());
            softAssertions.assertThat(clientDto.getFirstName()).isEqualTo(clientByPhoneDto.getFirstName());
            softAssertions.assertThat(clientDto.getMiddleName()).isEqualTo(clientByPhoneDto.getMiddleName());
            softAssertions.assertThat(clientDto.getLastName()).isEqualTo(clientByPhoneDto.getLastName());
            softAssertions.assertThat(clientDto.getAccountNumber()).isEqualTo(account.getAccountNumber());
        });
    }

}
