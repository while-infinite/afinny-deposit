package by.afinny.deposit.repository;

import by.afinny.deposit.entity.Agreement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

@Repository
public interface AgreementRepository extends JpaRepository<Agreement, UUID> {

    Optional<Agreement> findAgreementByIdAndIsActiveTrue(UUID id);

    Optional<Agreement> findAgreementByAccountClientIdAndIdAndIsActiveTrue(UUID clientId, UUID id);

    Optional<Agreement> findByAccountClientIdAndId(UUID clientId, UUID id);

    List<Agreement> findByAccountClientIdAndIsActiveTrue(UUID clientId);
}
