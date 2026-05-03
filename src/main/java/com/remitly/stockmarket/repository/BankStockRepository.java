package com.remitly.stockmarket.repository;

import com.remitly.stockmarket.entity.BankStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BankStockRepository extends JpaRepository<BankStock, String> {
    
    // Lock the DB row during the transaction to ensure balance NEVER drops below zero
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<BankStock> findById(String name);
}
