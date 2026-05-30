package org.gabguimaraes.wallet.controller;

import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.gabguimaraes.wallet.controller.dto.BalanceResponse;
import org.gabguimaraes.wallet.controller.dto.CreateWalletRequest;
import org.gabguimaraes.wallet.controller.dto.WalletResponse;
import org.gabguimaraes.wallet.exception.WalletNotFoundException;
import org.gabguimaraes.wallet.model.Wallet;
import org.gabguimaraes.wallet.service.WalletService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/")
    public ResponseEntity<WalletResponse> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        Wallet wallet = walletService.createWallet(request.userId());
        WalletResponse response = new WalletResponse(wallet.getId(), wallet.getStatus().name(), wallet.getCreatedAt());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{walletId}/balance")
    public ResponseEntity<BalanceResponse> getBalance(@PathVariable UUID walletId) {
        Long balanceCents = walletService.getBalance(walletId);
        return ResponseEntity.ok(new BalanceResponse(balanceCents));
    }

    @GetMapping("/{walletId}/history")
    public ResponseEntity<BalanceResponse> getHistoricalBalance(
            @PathVariable UUID walletId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            OffsetDateTime timestamp
    ) {
        Long balanceCents = walletService.getHistoricalBalance(walletId, timestamp);
        return ResponseEntity.ok(new BalanceResponse(balanceCents));
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<String> handleWalletNotFoundException(WalletNotFoundException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Wallet not found");
    }
}
