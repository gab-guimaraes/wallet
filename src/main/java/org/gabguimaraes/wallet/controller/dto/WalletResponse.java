package org.gabguimaraes.wallet.controller.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record WalletResponse(UUID walletId, String status, OffsetDateTime createdAt) {
}
