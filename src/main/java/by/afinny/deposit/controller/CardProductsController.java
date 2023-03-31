package by.afinny.deposit.controller;

import by.afinny.deposit.dto.CardProductDto;
import by.afinny.deposit.service.CardProductsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("auth/cards-products")
@RequiredArgsConstructor
public class CardProductsController {

    private final CardProductsService cardProductsService;

    @GetMapping
    public ResponseEntity<List<CardProductDto>> getAllCardProducts() {
        List<CardProductDto> cardProductDtoList = cardProductsService.getAllCardProducts();
        return ResponseEntity.ok(cardProductDtoList);
    }
}
