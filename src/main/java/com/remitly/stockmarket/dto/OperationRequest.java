package com.remitly.stockmarket.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class OperationRequest {
    @NotNull(message = "Type cannot be null")
    @Pattern(regexp = "^(buy|sell)$", message = "Type must be 'buy' or 'sell'")
    private String type;
}
