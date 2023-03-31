package by.afinny.deposit.unit.service;

import by.afinny.deposit.dto.RequestNewCardDto;
import by.afinny.deposit.dto.kafka.ConsumerNewCardEvent;
import by.afinny.deposit.entity.Account;
import by.afinny.deposit.entity.Card;
import by.afinny.deposit.mapper.CardMapper;
import by.afinny.deposit.repository.AccountRepository;
import by.afinny.deposit.repository.CardRepository;
import by.afinny.deposit.repository.ProductRepository;
import by.afinny.deposit.service.impl.OrderServiceImpl;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import by.afinny.deposit.exception.EntityNotFoundException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class OrderServiceImplTest {

    private final UUID CLIENT_ID = UUID.randomUUID();
    private final String ACCOUNT_NUMBER = "accNum";

    @InjectMocks
    private OrderServiceImpl orderService;

    @Mock
    private CardMapper cardMapper;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private ProductRepository productRepository;

    private RequestNewCardDto requestNewCardDto;
    private ConsumerNewCardEvent consumerNewCardEvent;
    private Card card;
    private Account account;

    @BeforeEach
    public void setUp() {
        requestNewCardDto = RequestNewCardDto.builder()
                .productId(1337).build();
        consumerNewCardEvent = ConsumerNewCardEvent.builder()
                .holderName("holder Name")
                .accountNumber(ACCOUNT_NUMBER).build();
        account = Account.builder()
                .accountNumber(ACCOUNT_NUMBER).build();
        card = Card.builder()
                .holderName("hold na")
                .account(account).build();
    }

    @Test
    @DisplayName("If product was not found then throw")
    void orderNewCard_ifNotSuccess_thenThrow(){
        //ARRANGE
        when(productRepository.findProductById(requestNewCardDto.getProductId())).thenReturn(Optional.empty());

        //ACT
        ThrowableAssert.ThrowingCallable orderNewCardInvocation = () ->
                orderService.orderNewCard(CLIENT_ID, requestNewCardDto);

        //VERIFY
        assertThatThrownBy(orderNewCardInvocation).isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("If success then save")
    void createNewCard_shouldSave() {
        //ARRANGE
        when(cardMapper.toCard(consumerNewCardEvent))
                .thenReturn(card);
        when(accountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                .thenReturn(Optional.of(account));

        //ACT
        orderService.createNewCard(consumerNewCardEvent);

        //VERIFY
        verify(cardRepository)
                .save(card);
        assertThat(card.getAccount())
                .isNotNull()
                .isEqualTo(account);
    }

    @Test
    @DisplayName("If Entity not found then throw Entity Not Found")
    void createNewCard_ifNotFound_thenThrow() {
        //ARRANGE
        when(cardMapper.toCard(consumerNewCardEvent))
                .thenReturn(card);
        when(accountRepository.findByAccountNumber(ACCOUNT_NUMBER))
                .thenReturn(Optional.empty());

        //ACT
        ThrowableAssert.ThrowingCallable getActiveAccountsMethod = () -> orderService.createNewCard(consumerNewCardEvent);

        //VERIFY
        assertThatThrownBy(getActiveAccountsMethod).isInstanceOf(EntityNotFoundException.class);

    }
}
