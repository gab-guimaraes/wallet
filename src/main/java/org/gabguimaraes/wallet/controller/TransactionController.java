package org.gabguimaraes.wallet.controller;

import java.util.UUID;

import org.gabguimaraes.wallet.controller.dto.DepositRequest;
import org.gabguimaraes.wallet.controller.dto.TransferRequest;
import org.gabguimaraes.wallet.controller.dto.WithdrawRequest;
import org.gabguimaraes.wallet.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/deposit/{walletId}")
    public ResponseEntity<Void> deposit(@PathVariable UUID walletId, @RequestBody DepositRequest request) {
        transactionService.deposit(walletId, request.amountCents(), request.idempotencyKey());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/withdraw/{walletId}")
    public ResponseEntity<Void> withdraw(@PathVariable UUID walletId, @RequestBody WithdrawRequest request) {
        transactionService.withdraw(walletId, request.amountCents(), request.idempotencyKey());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/transfer")
    public ResponseEntity<Void> transfer(@RequestBody TransferRequest request) {
        transactionService.transfer(
                request.fromWalletId(),
                request.toWalletId(),
                request.amountCents(),
                request.idempotencyKey()
        );
        return ResponseEntity.ok().build();
    }
}
