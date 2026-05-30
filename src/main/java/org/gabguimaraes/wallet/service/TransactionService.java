package org.gabguimaraes.wallet.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import org.gabguimaraes.wallet.exception.InsufficientBalanceException;
import org.gabguimaraes.wallet.exception.WalletNotFoundException;
import org.gabguimaraes.wallet.model.LedgerEntry;
import org.gabguimaraes.wallet.model.LedgerEntryType;
import org.gabguimaraes.wallet.model.Wallet;
import org.gabguimaraes.wallet.model.WalletBalance;
import org.gabguimaraes.wallet.repository.LedgerEntryRepository;
import org.gabguimaraes.wallet.repository.WalletBalanceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final WalletBalanceRepository walletBalanceRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final IdempotencyService idempotencyService;

    public TransactionService(
            WalletBalanceRepository walletBalanceRepository,
            LedgerEntryRepository ledgerEntryRepository,
            IdempotencyService idempotencyService
    ) {
        this.walletBalanceRepository = walletBalanceRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.idempotencyService = idempotencyService;
    }

    /**
     * Deposits funds into the wallet.
     *
     * @param walletId the target wallet id
     * @param amountCents the amount to add in cents
     * @param idempotencyKey the idempotency key
     */
    @Transactional
    public void deposit(UUID walletId, long amountCents, UUID idempotencyKey) {
        idempotencyService.startProcessing(idempotencyKey);

        try {
            validatePositiveAmount(amountCents);

            OffsetDateTime now = OffsetDateTime.now();
            WalletBalance walletBalance = lockWalletBalance(walletId);

            long balanceBefore = walletBalance.getBalanceCents();
            long balanceAfter = balanceBefore + amountCents;

            walletBalance.setBalanceCents(balanceAfter);
            walletBalance.setUpdatedAt(now);
            walletBalanceRepository.save(walletBalance);

            UUID operationId = UUID.randomUUID();
            LedgerEntry ledgerEntry = newLedgerEntry(
                    operationId,
                    walletBalance.getWallet(),
                    LedgerEntryType.DEPOSIT,
                    amountCents,
                    balanceBefore,
                    balanceAfter,
                    now
            );
            ledgerEntryRepository.save(ledgerEntry);

            idempotencyService.complete(idempotencyKey, successResponse(operationId));
        } catch (RuntimeException exception) {
            idempotencyService.fail(idempotencyKey);
            throw exception;
        }
    }

    /**
     * Withdraws funds from the wallet.
     *
     * @param walletId the source wallet id
     * @param amountCents the amount to subtract in cents
     * @param idempotencyKey the idempotency key
     */
    @Transactional
    public void withdraw(UUID walletId, long amountCents, UUID idempotencyKey) {
        idempotencyService.startProcessing(idempotencyKey);

        try {
            validatePositiveAmount(amountCents);

            OffsetDateTime now = OffsetDateTime.now();
            WalletBalance walletBalance = lockWalletBalance(walletId);

            long balanceBefore = walletBalance.getBalanceCents();
            if (balanceBefore < amountCents) {
                throw new InsufficientBalanceException(walletId, balanceBefore, amountCents);
            }
            long balanceAfter = balanceBefore - amountCents;

            walletBalance.setBalanceCents(balanceAfter);
            walletBalance.setUpdatedAt(now);
            walletBalanceRepository.save(walletBalance);

            UUID operationId = UUID.randomUUID();
            LedgerEntry ledgerEntry = newLedgerEntry(
                    operationId,
                    walletBalance.getWallet(),
                    LedgerEntryType.WITHDRAW,
                    amountCents,
                    balanceBefore,
                    balanceAfter,
                    now
            );
            ledgerEntryRepository.save(ledgerEntry);

            idempotencyService.complete(idempotencyKey, successResponse(operationId));
        } catch (RuntimeException exception) {
            idempotencyService.fail(idempotencyKey);
            throw exception;
        }
    }

    /**
     * Transfers funds between wallets.
     *
     * @param fromWalletId the origin wallet id
     * @param toWalletId the target wallet id
     * @param amountCents the transfer amount in cents
     * @param idempotencyKey the idempotency key
     */
    @Transactional
    public void transfer(UUID fromWalletId, UUID toWalletId, long amountCents, UUID idempotencyKey) {
        idempotencyService.startProcessing(idempotencyKey);

        try {
            validatePositiveAmount(amountCents);
            if (fromWalletId.equals(toWalletId)) {
                throw new IllegalArgumentException("Source and target wallets must be different");
            }

            // Lock always in deterministic order to avoid deadlocks.
            UUID firstWalletId = fromWalletId.compareTo(toWalletId) <= 0 ? fromWalletId : toWalletId;
            UUID secondWalletId = fromWalletId.compareTo(toWalletId) <= 0 ? toWalletId : fromWalletId;

            WalletBalance firstLocked = lockWalletBalance(firstWalletId);
            WalletBalance secondLocked = lockWalletBalance(secondWalletId);

            WalletBalance fromWalletBalance = fromWalletId.equals(firstWalletId) ? firstLocked : secondLocked;
            WalletBalance toWalletBalance = fromWalletId.equals(firstWalletId) ? secondLocked : firstLocked;

            long fromBefore = fromWalletBalance.getBalanceCents();
            if (fromBefore < amountCents) {
                throw new InsufficientBalanceException(fromWalletId, fromBefore, amountCents);
            }

            long fromAfter = fromBefore - amountCents;
            long toBefore = toWalletBalance.getBalanceCents();
            long toAfter = toBefore + amountCents;

            OffsetDateTime now = OffsetDateTime.now();
            fromWalletBalance.setBalanceCents(fromAfter);
            fromWalletBalance.setUpdatedAt(now);
            toWalletBalance.setBalanceCents(toAfter);
            toWalletBalance.setUpdatedAt(now);
            walletBalanceRepository.saveAll(List.of(fromWalletBalance, toWalletBalance));

            UUID operationId = UUID.randomUUID();
            LedgerEntry transferOutEntry = newLedgerEntry(
                    operationId,
                    fromWalletBalance.getWallet(),
                    LedgerEntryType.TRANSFER_OUT,
                    amountCents,
                    fromBefore,
                    fromAfter,
                    now
            );
            LedgerEntry transferInEntry = newLedgerEntry(
                    operationId,
                    toWalletBalance.getWallet(),
                    LedgerEntryType.TRANSFER_IN,
                    amountCents,
                    toBefore,
                    toAfter,
                    now
            );
            ledgerEntryRepository.saveAll(List.of(transferOutEntry, transferInEntry));

            idempotencyService.complete(idempotencyKey, successResponse(operationId));
        } catch (RuntimeException exception) {
            idempotencyService.fail(idempotencyKey);
            throw exception;
        }
    }

    private WalletBalance lockWalletBalance(UUID walletId) {
        return walletBalanceRepository.findByWalletIdForUpdate(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId));
    }

    private void validatePositiveAmount(long amountCents) {
        if (amountCents <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }

    private LedgerEntry newLedgerEntry(
            UUID operationId,
            Wallet wallet,
            LedgerEntryType type,
            long amountCents,
            long balanceBefore,
            long balanceAfter,
            OffsetDateTime createdAt
    ) {
        LedgerEntry ledgerEntry = new LedgerEntry();
        ledgerEntry.setId(UUID.randomUUID());
        ledgerEntry.setOperationId(operationId);
        ledgerEntry.setWallet(wallet);
        ledgerEntry.setType(type);
        ledgerEntry.setAmountCents(amountCents);
        ledgerEntry.setBalanceBeforeCents(balanceBefore);
        ledgerEntry.setBalanceAfterCents(balanceAfter);
        ledgerEntry.setCreatedAt(createdAt);
        return ledgerEntry;
    }

    private String successResponse(UUID operationId) {
        return "{\"operationId\":\"" + operationId + "\"}";
    }
}
