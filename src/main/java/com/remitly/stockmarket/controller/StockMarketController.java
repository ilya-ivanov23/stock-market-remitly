package com.remitly.stockmarket.controller;

import com.remitly.stockmarket.dto.*;
import com.remitly.stockmarket.service.StockMarketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class StockMarketController {

    private final StockMarketService service;

    @PostMapping("/wallets/{wallet_id}/stocks/{stock_name}")
    public ResponseEntity<Void> processOperation(
            @PathVariable("wallet_id") String walletId,
            @PathVariable("stock_name") String stockName,
            @Valid @RequestBody OperationRequest request) {
        service.processOperation(walletId, stockName, request.getType());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/wallets/{wallet_id}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable("wallet_id") String walletId) {
        return ResponseEntity.ok(service.getWalletState(walletId));
    }

    @GetMapping("/wallets/{wallet_id}/stocks/{stock_name}")
    public ResponseEntity<Long> getWalletStockQuantity(
            @PathVariable("wallet_id") String walletId,
            @PathVariable("stock_name") String stockName) {
        return ResponseEntity.ok(service.getWalletStockQuantity(walletId, stockName));
    }

    @GetMapping("/stocks")
    public ResponseEntity<BankStateDto> getBankState() {
        return ResponseEntity.ok(service.getBankState());
    }

    @PostMapping("/stocks")
    public ResponseEntity<Void> setBankState(@RequestBody BankStateDto stateDto) {
        service.setBankState(stateDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/log")
    public ResponseEntity<AuditLogResponse> getAuditLog() {
        return ResponseEntity.ok(service.getAuditLog());
    }
}
