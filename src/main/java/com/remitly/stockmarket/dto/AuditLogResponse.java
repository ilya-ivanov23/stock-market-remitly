package com.remitly.stockmarket.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class AuditLogResponse {
    private List<Map<String, String>> log;
}
