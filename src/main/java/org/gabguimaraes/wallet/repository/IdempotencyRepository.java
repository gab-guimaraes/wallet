package org.gabguimaraes.wallet.repository;

import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;
import org.gabguimaraes.wallet.model.IdempotencyRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;


public interface IdempotencyRepository extends JpaRepository<IdempotencyRequest, UUID> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<IdempotencyRequest> findById(UUID id);
}