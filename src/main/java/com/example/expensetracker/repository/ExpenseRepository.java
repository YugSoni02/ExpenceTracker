package com.example.expensetracker.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.expensetracker.model.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

	List<Expense> findAllByOrderByDateDescIdDesc();

	@Query("SELECT SUM(e.amount) FROM Expense e WHERE e.category = :category")
	BigDecimal getTotalSpendingByCategory(@Param("category") String category);
}
