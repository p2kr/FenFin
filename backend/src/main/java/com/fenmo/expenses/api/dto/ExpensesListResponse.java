package com.fenmo.expenses.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ExpensesListResponse {
    private List<ExpenseResponse> items;
    private String total; // Sent as String
}
