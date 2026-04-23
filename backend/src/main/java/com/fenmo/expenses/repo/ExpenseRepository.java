package com.fenmo.expenses.repo;

import com.fenmo.expenses.domain.Expense;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {
    
    Optional<Expense> findByIdempotencyKey(String idempotencyKey);
    
    @Query("SELECT e FROM Expense e WHERE (:category IS NULL OR e.category = :category)")
    List<Expense> findByCategoryWithSort(@Param("category") String category, Sort sort);
}
