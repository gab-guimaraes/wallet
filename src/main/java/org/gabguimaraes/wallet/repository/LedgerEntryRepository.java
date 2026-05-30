package org.gabguimaraes.wallet.repository;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.gabguimaraes.wallet.model.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    Optional<LedgerEntry> findFirstByWallet_IdAndCreatedAtLessThanEqualOrderByCreatedAtDesc(
            UUID walletId,
            OffsetDateTime timestamp
    );
}
