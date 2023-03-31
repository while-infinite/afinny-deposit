package by.afinny.deposit.mapper;

import by.afinny.deposit.dto.ActiveDepositDto;
import by.afinny.deposit.dto.DepositDto;
import by.afinny.deposit.dto.RefillDebitCardDto;
import by.afinny.deposit.entity.Account;
import by.afinny.deposit.entity.Agreement;
import by.afinny.deposit.entity.Card;
import by.afinny.deposit.entity.Operation;
import by.afinny.deposit.entity.Product;
import by.afinny.deposit.exception.DefaultCardNotFoundException;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface DepositMapper {

    @Mapping(source = "card.cardNumber", target = "cardNumber")
    @Mapping(source = "product.name", target = "name")
    @Mapping(source = "product.currencyCode", target = "currencyCode")
    @Mapping(source = "product.schemaName", target = "schemaName")
    @Mapping(source = "product.isCapitalization", target = "isCapitalization")
    @Mapping(source = "product.isRevocable", target = "isRevocable")
    DepositDto toDepositDto(Agreement agreement, Product product, Card card);

    List<ActiveDepositDto> toActiveDepositsDto(List<Agreement> agreements);

    @Mappings({
            @Mapping(source = "agreement.product.name", target = "productName"),
            @Mapping(source = "agreement.product.currencyCode", target = "currencyCode"),
            @Mapping(source = "agreement", target = "cardNumber", qualifiedByName = "getDefaultCard"),
            @Mapping(source = "agreement.id", target = "agreementId")})
    ActiveDepositDto toActiveDepositDto(Agreement agreement);

    @Mapping(target = "sum", qualifiedByName = "stringToBigDecimal")
    @Mapping(target = "completedAt", source = "completedAt")
    @Mapping(target = "account", source = "account")
    Operation refillDebitCardDtoToOperation(RefillDebitCardDto refillDebitCardDto, Account account, LocalDateTime completedAt);

    @Named("getDefaultCard")
    default String getDefaultCard(Agreement agreement) {

        return agreement.getAccount().getCards().stream()
                .filter(Card::getIsDefault)
                .findFirst()
                .orElseThrow(() -> new DefaultCardNotFoundException(
                        "Cannot find default card"))
                .getCardNumber();
    }

    @Named("stringToBigDecimal")
    default BigDecimal stringToBigDecimal(String sum) {
        return new BigDecimal(sum);
    }
}

