package com.fenmo.expenses.api.dto;

import com.fenmo.expenses.domain.Expense;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.UUID;

@Data
@Builder
public class ExpenseResponse {
    private UUID id;
    private String amount; // Sent as String to preserve exact precision in JSON
    private String category;
    private String description;
    private LocalDate date;
    private ZonedDateTime createdAt;

    public static ExpenseResponse fromEntity(Expense entity) {
        return ExpenseResponse.builder()
                .id(entity.getId())
                .amount(entity.getAmount().toPlainString())
                .category(entity.getCategory())
                .description(entity.getDescription())
                .date(entity.getDate())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
