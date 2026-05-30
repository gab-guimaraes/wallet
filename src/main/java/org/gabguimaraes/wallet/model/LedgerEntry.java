package org.gabguimaraes.wallet.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "ledger_entry",
        indexes = {
                @Index(name = "idx_ledger_entry_wallet_created_at_desc", columnList = "wallet_id, created_at DESC"),
                @Index(name = "idx_ledger_entry_operation_id", columnList = "operation_id")
        }
)
public class LedgerEntry {

    @Id
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "operation_id", nullable = false)
    private UUID operationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private LedgerEntryType type;

    @Column(name = "amount_cents", nullable = false)
    private Long amountCents;

    @Column(name = "balance_before_cents", nullable = false)
    private Long balanceBeforeCents;

    @Column(name = "balance_after_cents", nullable = false)
    private Long balanceAfterCents;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    public LedgerEntry() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getOperationId() {
        return operationId;
    }

    public void setOperationId(UUID operationId) {
        this.operationId = operationId;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

    public LedgerEntryType getType() {
        return type;
    }

    public void setType(LedgerEntryType type) {
        this.type = type;
    }

    public Long getAmountCents() {
        return amountCents;
    }

    public void setAmountCents(Long amountCents) {
        this.amountCents = amountCents;
    }

    public Long getBalanceBeforeCents() {
        return balanceBeforeCents;
    }

    public void setBalanceBeforeCents(Long balanceBeforeCents) {
        this.balanceBeforeCents = balanceBeforeCents;
    }

    public Long getBalanceAfterCents() {
        return balanceAfterCents;
    }

    public void setBalanceAfterCents(Long balanceAfterCents) {
        this.balanceAfterCents = balanceAfterCents;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
