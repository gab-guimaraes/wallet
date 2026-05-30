package org.gabguimaraes.wallet.controller.dto;

import java.util.UUID;

public record WithdrawRequest(long amountCents, UUID idempotencyKey) {
}
