package com.fenmo.expenses.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fenmo.expenses.api.dto.CreateExpenseRequest;
import com.fenmo.expenses.api.dto.ExpenseResponse;
import com.fenmo.expenses.api.dto.ExpensesListResponse;
import com.fenmo.expenses.api.dto.SummaryResponse;
import com.fenmo.expenses.domain.Expense;
import com.fenmo.expenses.repo.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository repository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.SERIALIZABLE)
    public ExpenseResponse createExpense(String idempotencyKey, CreateExpenseRequest request) {
        String requestHash = computeHash(request);

        Optional<Expense> existing = repository.findByIdempotencyKey(idempotencyKey);
        if (existing.isPresent()) {
            Expense expense = existing.get();
            if (!expense.getRequestHash().equals(requestHash)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Idempotency key reused with different payload");
            }
            return ExpenseResponse.fromEntity(expense);
        }

        Expense newExpense = Expense.builder()
                .amount(request.getAmount())
                .category(request.getCategory())
                .description(request.getDescription())
                .date(request.getDate())
                .idempotencyKey(idempotencyKey)
                .requestHash(requestHash)
                .build();

        try {
            Expense saved = repository.saveAndFlush(newExpense);
            return ExpenseResponse.fromEntity(saved);
        } catch (DataIntegrityViolationException e) {
            // In case of a race condition on unique index, re-read and apply the same hit logic
            existing = repository.findByIdempotencyKey(idempotencyKey);
            if (existing.isPresent()) {
                Expense expense = existing.get();
                if (!expense.getRequestHash().equals(requestHash)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Idempotency key reused with different payload");
                }
                return ExpenseResponse.fromEntity(expense);
            }
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public ExpensesListResponse getExpenses(String category, String sortParam) {
        Sort.Direction direction = "date_asc".equals(sortParam) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, "date").and(Sort.by(Sort.Direction.DESC, "createdAt"));
        
        List<Expense> expenses = repository.findByCategoryWithSort(category, sort);
        
        BigDecimal total = expenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return ExpensesListResponse.builder()
                .items(expenses.stream().map(ExpenseResponse::fromEntity).toList())
                .total(total.toPlainString())
                .build();
    }

    @Transactional(readOnly = true)
    public SummaryResponse getSummary() {
        List<Expense> allExpenses = repository.findAll();
        
        BigDecimal grandTotal = allExpenses.stream()
                .map(Expense::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        Map<String, BigDecimal> byCategory = allExpenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Expense::getAmount, BigDecimal::add)
                ));
                
        List<SummaryResponse.CategoryTotal> categoryTotals = byCategory.entrySet().stream()
                .map(entry -> SummaryResponse.CategoryTotal.builder()
                        .category(entry.getKey())
                        .total(entry.getValue().toPlainString())
                        .build())
                .toList();

        return SummaryResponse.builder()
                .byCategory(categoryTotals)
                .grandTotal(grandTotal.toPlainString())
                .build();
    }

    private String computeHash(CreateExpenseRequest request) {
        try {
            // Convert to a predictable JSON format or simple concatenated string
            String canonicalStr = objectMapper.writeValueAsString(request);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(canonicalStr.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (JsonProcessingException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to compute request hash", e);
        }
    }
}
