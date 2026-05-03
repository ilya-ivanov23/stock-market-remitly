package com.remitly.stockmarket.service;

import com.remitly.stockmarket.dto.*;
import com.remitly.stockmarket.entity.*;
import com.remitly.stockmarket.exception.*;
import com.remitly.stockmarket.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockMarketService {

    private final BankStockRepository bankStockRepository;
    private final WalletStockRepository walletStockRepository;
    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void processOperation(String walletId, String stockName, String type) {
        // 1. Check if stock exists
        BankStock bankStock = bankStockRepository.findById(stockName)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found"));

        if ("buy".equalsIgnoreCase(type)) {
            // Return 400 if bank has no stocks
            if (bankStock.getQuantity() <= 0) {
                throw new BadRequestException("No stock in the bank");
            }

            // Decrease in bank, increase/create in wallet
            bankStock.setQuantity(bankStock.getQuantity() - 1);
            
            WalletStock walletStock = walletStockRepository.findByWalletIdAndStockName(walletId, stockName)
                    .orElse(new WalletStock(null, walletId, stockName, 0L));
            walletStock.setQuantity(walletStock.getQuantity() + 1);
            walletStockRepository.save(walletStock);

        } else if ("sell".equalsIgnoreCase(type)) {
            // Find stock in wallet
            WalletStock walletStock = walletStockRepository.findByWalletIdAndStockName(walletId, stockName)
                    .orElseThrow(() -> new BadRequestException("No stock in the wallet"));

            // Return 400 if wallet has 0 or less
            if (walletStock.getQuantity() <= 0) {
                throw new BadRequestException("No stock in the wallet");
            }

            // Decrease in wallet, increase in bank
            walletStock.setQuantity(walletStock.getQuantity() - 1);
            bankStock.setQuantity(bankStock.getQuantity() + 1);
            walletStockRepository.save(walletStock);
        }

        // Save logs (only if operation is successful)
        auditLogRepository.save(new AuditLog(null, type.toLowerCase(), walletId, stockName, null));
        bankStockRepository.save(bankStock);
    }

    public WalletResponse getWalletState(String walletId) {
        List<StockDto> stocks = walletStockRepository.findAllByWalletId(walletId).stream()
                .filter(ws -> ws.getQuantity() > 0) // Show only available stocks
                .map(ws -> new StockDto(ws.getStockName(), ws.getQuantity()))
                .collect(Collectors.toList());
        return new WalletResponse(walletId, stocks);
    }

    public Long getWalletStockQuantity(String walletId, String stockName) {
        return walletStockRepository.findByWalletIdAndStockName(walletId, stockName)
                .map(WalletStock::getQuantity)
                .orElse(0L); // Return 0 if not found
    }

    public BankStateDto getBankState() {
        List<StockDto> stocks = bankStockRepository.findAll().stream()
                .map(bs -> new StockDto(bs.getName(), bs.getQuantity()))
                .collect(Collectors.toList());
        return new BankStateDto(stocks);
    }

    @Transactional
    public void setBankState(BankStateDto stateDto) {
        bankStockRepository.deleteAll();
        List<BankStock> newStocks = stateDto.getStocks().stream()
                .map(dto -> new BankStock(dto.getName(), dto.getQuantity()))
                .collect(Collectors.toList());
        bankStockRepository.saveAll(newStocks);
    }

    public AuditLogResponse getAuditLog() {
        List<Map<String, String>> logList = auditLogRepository.findAllByOrderByIdAsc().stream()
                .map(log -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("type", log.getType());
                    map.put("wallet_id", log.getWalletId());
                    map.put("stock_name", log.getStockName());
                    return map;
                })
                .collect(Collectors.toList());
        return new AuditLogResponse(logList);
    }
}
