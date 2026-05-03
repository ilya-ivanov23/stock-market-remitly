package com.remitly.stockmarket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class WalletResponse {
    private String id;
    private List<StockDto> stocks;
}
