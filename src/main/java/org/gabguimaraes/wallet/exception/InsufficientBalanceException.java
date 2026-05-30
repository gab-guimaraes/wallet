package org.gabguimaraes.wallet.exception;

import java.util.UUID;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(UUID walletId, long availableBalance, long requestedAmount) {
        super("Insufficient balance for wallet " + walletId
                + ". Available: " + availableBalance
                + ", requested: " + requestedAmount);
    }
}
