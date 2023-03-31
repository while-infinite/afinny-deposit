package by.afinny.deposit.mapper;

import by.afinny.deposit.dto.kafka.ConsumerWithdrawEvent;
import by.afinny.deposit.entity.Operation;
import org.mapstruct.Mapper;

@Mapper
public interface OperationMapper {

    Operation consumerWithdrawEventToOperation(ConsumerWithdrawEvent consumerWithdrawEvent);
}
