package by.afinny.deposit.controller;

import by.afinny.deposit.dto.ProductDto;
import by.afinny.deposit.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("auth/deposit-products")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductDto>> getActiveDepositProducts() {
        List<ProductDto> productDtoList = productService.getActiveDepositProducts();
        return ResponseEntity.ok(productDtoList);
    }
}
