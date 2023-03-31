package by.afinny.deposit.service;

import by.afinny.deposit.dto.ProductDto;

import java.util.List;

public interface ProductService {

    List<ProductDto> getActiveDepositProducts();
}
