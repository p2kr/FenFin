package com.fenmo.expenses.api;

import com.fenmo.expenses.api.dto.CreateExpenseRequest;
import com.fenmo.expenses.api.dto.ExpenseResponse;
import com.fenmo.expenses.api.dto.ExpensesListResponse;
import com.fenmo.expenses.api.dto.SummaryResponse;
import com.fenmo.expenses.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService service;

    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(
            @RequestHeader(value = "Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CreateExpenseRequest request) {
        
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        ExpenseResponse response = service.createExpense(idempotencyKey, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<ExpensesListResponse> getExpenses(
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "date_desc") String sort) {
        return ResponseEntity.ok(service.getExpenses(category, sort));
    }

    @GetMapping("/summary")
    public ResponseEntity<SummaryResponse> getSummary() {
        return ResponseEntity.ok(service.getSummary());
    }
}
