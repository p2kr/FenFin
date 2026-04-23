package com.fenmo.expenses.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SummaryResponse {
    private List<CategoryTotal> byCategory;
    private String grandTotal;

    @Data
    @Builder
    public static class CategoryTotal {
        private String category;
        private String total;
    }
}
