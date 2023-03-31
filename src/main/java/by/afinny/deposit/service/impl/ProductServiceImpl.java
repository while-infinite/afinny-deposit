package by.afinny.deposit.service.impl;

import by.afinny.deposit.dto.ProductDto;
import by.afinny.deposit.entity.Product;
import by.afinny.deposit.mapper.ProductMapper;
import by.afinny.deposit.repository.ProductRepository;
import by.afinny.deposit.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @Override
    @Cacheable(value = "allActiveDepositProducts")
    public List<ProductDto> getActiveDepositProducts() {
        log.info("getActiveDepositProducts() method invoked.");
        List<Product> activeProducts = productRepository.findByIsActiveTrue();
        return productMapper.productsToProductsDto(activeProducts);
    }
}
