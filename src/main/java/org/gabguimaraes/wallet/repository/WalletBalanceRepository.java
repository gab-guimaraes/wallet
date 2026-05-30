package org.gabguimaraes.wallet.repository;

import jakarta.persistence.LockModeType;
import java.util.UUID;
import org.gabguimaraes.wallet.model.WalletBalance;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WalletBalanceRepository extends JpaRepository<WalletBalance, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select wb from WalletBalance wb where wb.walletId = :walletId")
    Optional<WalletBalance> findByWalletIdForUpdate(@Param("walletId") UUID walletId);
}
