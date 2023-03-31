package by.afinny.deposit.repository;

import by.afinny.deposit.entity.CardProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CardProductRepository extends JpaRepository<CardProduct, Integer> {

    List<CardProduct> findAllByIsActiveTrue();
}
