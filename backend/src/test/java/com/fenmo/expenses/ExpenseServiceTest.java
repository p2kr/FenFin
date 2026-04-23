package com.fenmo.expenses;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fenmo.expenses.api.dto.CreateExpenseRequest;
import com.fenmo.expenses.api.dto.ExpenseResponse;
import com.fenmo.expenses.domain.Expense;
import com.fenmo.expenses.repo.ExpenseRepository;
import com.fenmo.expenses.service.ExpenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ExpenseServiceTest {

    private ExpenseRepository repository;
    private ObjectMapper objectMapper;
    private ExpenseService service;

    @BeforeEach
    void setUp() {
        repository = mock(ExpenseRepository.class);
        objectMapper = new ObjectMapper(); // Use real mapper for hash predictability
        service = new ExpenseService(repository, objectMapper);
    }

    @Test
    void testCreateExpense_New() {
        String key = "key1";
        CreateExpenseRequest req = new CreateExpenseRequest();
        req.setAmount(new BigDecimal("10.00"));
        req.setCategory("food");
        req.setDescription("lunch");
        req.setDate(LocalDate.now());

        when(repository.findByIdempotencyKey(key)).thenReturn(Optional.empty());
        
        Expense savedExpense = new Expense();
        savedExpense.setId(UUID.randomUUID());
        savedExpense.setAmount(req.getAmount());
        savedExpense.setCategory(req.getCategory());
        savedExpense.setDescription(req.getDescription());
        savedExpense.setDate(req.getDate());
        
        when(repository.saveAndFlush(any())).thenReturn(savedExpense);

        ExpenseResponse response = service.createExpense(key, req);

        assertNotNull(response);
        assertEquals("10.00", response.getAmount());
        
        ArgumentCaptor<Expense> captor = ArgumentCaptor.forClass(Expense.class);
        verify(repository).saveAndFlush(captor.capture());
        assertEquals(key, captor.getValue().getIdempotencyKey());
        assertNotNull(captor.getValue().getRequestHash());
    }

    @Test
    void testCreateExpense_IdempotentHit() {
        String key = "key1";
        CreateExpenseRequest req = new CreateExpenseRequest();
        req.setAmount(new BigDecimal("10.00"));
        req.setCategory("food");
        req.setDescription("lunch");
        req.setDate(LocalDate.now());

        // Calculate expected hash
        String hash = "";
        try {
            String json = objectMapper.writeValueAsString(req);
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            hash = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        } catch (Exception e) {}

        Expense existing = new Expense();
        existing.setId(UUID.randomUUID());
        existing.setAmount(new BigDecimal("10.00"));
        existing.setCategory("food");
        existing.setDescription("lunch");
        existing.setDate(req.getDate());
        existing.setRequestHash(hash);
        existing.setIdempotencyKey(key);

        when(repository.findByIdempotencyKey(key)).thenReturn(Optional.of(existing));

        ExpenseResponse response = service.createExpense(key, req);

        assertNotNull(response);
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void testCreateExpense_Conflict() {
        String key = "key1";
        CreateExpenseRequest req = new CreateExpenseRequest();
        req.setAmount(new BigDecimal("10.00"));
        req.setCategory("food");
        req.setDescription("lunch");
        req.setDate(LocalDate.now());

        Expense existing = new Expense();
        existing.setId(UUID.randomUUID());
        existing.setRequestHash("different-hash");
        existing.setIdempotencyKey(key);

        when(repository.findByIdempotencyKey(key)).thenReturn(Optional.of(existing));

        assertThrows(ResponseStatusException.class, () -> service.createExpense(key, req));
    }
}
