package by.afinny.deposit.controller;

import by.afinny.deposit.dto.RequestNewCardDto;
import by.afinny.deposit.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("auth/deposit-card-orders")
@RequiredArgsConstructor
public class OrderController {

    public static final String URL_CARD_ORDER = "/auth/deposit-card-orders";
    public static final String URL_ORDER_NEW = "/new";
    public static final String PARAM_CLIENT_ID = "clientId";

    private final OrderService orderService;

    @PostMapping("new")
    public void orderNewCard(@RequestParam UUID clientId, @RequestBody RequestNewCardDto requestNewCardDto) {
        orderService.orderNewCard(clientId, requestNewCardDto);
    }
}
