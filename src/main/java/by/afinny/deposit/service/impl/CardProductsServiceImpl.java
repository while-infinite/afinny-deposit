package by.afinny.deposit.service.impl;

import by.afinny.deposit.dto.CardProductDto;
import by.afinny.deposit.entity.CardProduct;
import by.afinny.deposit.mapper.CardProductMapper;
import by.afinny.deposit.repository.CardProductRepository;
import by.afinny.deposit.service.CardProductsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardProductsServiceImpl implements CardProductsService {

    private final CardProductRepository cardProductRepository;
    private final CardProductMapper cardProductMapper;

    @Override
    @Cacheable(value = "allCardProducts")
    public List<CardProductDto> getAllCardProducts() {
        log.info("getAllCardProducts() method invoke");

        List<CardProduct> cardProducts = cardProductRepository.findAllByIsActiveTrue();
        return cardProductMapper.toCardProductDtoList(cardProducts);
    }
}
