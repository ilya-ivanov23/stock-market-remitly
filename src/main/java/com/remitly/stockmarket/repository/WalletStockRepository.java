package com.remitly.stockmarket.repository;

import com.remitly.stockmarket.entity.WalletStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletStockRepository extends JpaRepository<WalletStock, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<WalletStock> findByWalletIdAndStockName(String walletId, String stockName);
    
    List<WalletStock> findAllByWalletId(String walletId);
}
