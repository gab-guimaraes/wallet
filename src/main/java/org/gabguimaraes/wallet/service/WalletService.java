package org.gabguimaraes.wallet.service;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.gabguimaraes.wallet.exception.WalletNotFoundException;
import org.gabguimaraes.wallet.model.LedgerEntry;
import org.gabguimaraes.wallet.model.Wallet;
import org.gabguimaraes.wallet.model.WalletBalance;
import org.gabguimaraes.wallet.model.WalletStatus;
import org.gabguimaraes.wallet.repository.LedgerEntryRepository;
import org.gabguimaraes.wallet.repository.WalletBalanceRepository;
import org.gabguimaraes.wallet.repository.WalletRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final WalletBalanceRepository walletBalanceRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public WalletService(
            WalletRepository walletRepository,
            WalletBalanceRepository walletBalanceRepository,
            LedgerEntryRepository ledgerEntryRepository
    ) {
        this.walletRepository = walletRepository;
        this.walletBalanceRepository = walletBalanceRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    /**
     * Creates a new wallet and its initial balance row.
     *
     * @param userId the owner user id
     * @return the persisted wallet
     */
    @Transactional
    public Wallet createWallet(UUID userId) {
        OffsetDateTime now = OffsetDateTime.now();

        Wallet wallet = new Wallet();
        wallet.setId(UUID.randomUUID());
        wallet.setUserId(userId);
        wallet.setStatus(WalletStatus.ACTIVE);
        wallet.setCreatedAt(now);
        wallet.setUpdatedAt(now);

        WalletBalance walletBalance = new WalletBalance();
        walletBalance.setWallet(wallet);
        walletBalance.setBalanceCents(0L);
        walletBalance.setUpdatedAt(now);

        wallet.setWalletBalance(walletBalance);

        return walletRepository.save(wallet);
    }

    /**
     * Returns the current balance for a wallet id.
     *
     * @param walletId the wallet id
     * @return the current balance in cents
     */
    @Transactional(readOnly = true)
    public Long getBalance(UUID walletId) {
        WalletBalance walletBalance = walletBalanceRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
        return walletBalance.getBalanceCents();
    }

    /**
     * Returns the wallet balance at a given timestamp.
     *
     * @param walletId the wallet id
     * @param timestamp the point in time to query
     * @return the balance after the latest entry at or before the timestamp, or zero when no entries exist
     */
    @Transactional(readOnly = true)
    public Long getHistoricalBalance(UUID walletId, OffsetDateTime timestamp) {
        if (!walletRepository.existsById(walletId)) {
            throw new WalletNotFoundException(walletId);
        }

        return ledgerEntryRepository
                .findFirstByWallet_IdAndCreatedAtLessThanEqualOrderByCreatedAtDesc(walletId, timestamp)
                .map(LedgerEntry::getBalanceAfterCents)
                .orElse(0L);
    }
}
