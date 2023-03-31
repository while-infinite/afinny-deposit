package by.afinny.deposit.mapper;

import by.afinny.deposit.dto.CardProductDto;
import by.afinny.deposit.entity.CardProduct;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface CardProductMapper {

    List<CardProductDto> toCardProductDtoList(List<CardProduct> cardProducts);
}
