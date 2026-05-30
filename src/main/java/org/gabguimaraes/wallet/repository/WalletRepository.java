package org.gabguimaraes.wallet.repository;

import java.util.UUID;
import org.gabguimaraes.wallet.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, UUID> {
}
