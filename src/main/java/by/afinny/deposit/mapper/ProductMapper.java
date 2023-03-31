package by.afinny.deposit.mapper;

import by.afinny.deposit.dto.ProductDto;
import by.afinny.deposit.entity.Product;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper
public interface ProductMapper {

    List<ProductDto> productsToProductsDto(List<Product> products);
}
