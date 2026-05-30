package org.gabguimaraes.wallet.controller.dto;

import java.util.UUID;

public record TransferRequest(
        UUID fromWalletId,
        UUID toWalletId,
        long amountCents,
        UUID idempotencyKey
) {
}
