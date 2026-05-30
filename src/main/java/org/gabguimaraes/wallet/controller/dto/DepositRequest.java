package org.gabguimaraes.wallet.controller.dto;

import java.util.UUID;

public record DepositRequest(long amountCents, UUID idempotencyKey) {
}
