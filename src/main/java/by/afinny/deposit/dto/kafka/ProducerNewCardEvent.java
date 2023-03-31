package by.afinny.deposit.dto.kafka;

import by.afinny.deposit.dto.RequestNewCardDto;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Getter
@Setter(AccessLevel.PUBLIC)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ProducerNewCardEvent {

    private UUID clientId;
    private RequestNewCardDto requestNewCardDto;
}
