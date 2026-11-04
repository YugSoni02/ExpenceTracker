package com.example.expensetracker.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.expensetracker.exception.ResourceNotFoundException;
import com.example.expensetracker.model.Expense;
import com.example.expensetracker.repository.ExpenseRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenseService {

	private final ExpenseRepository expenseRepository;

	public List<Expense> findAll() {
		return expenseRepository.findAllByOrderByDateDescIdDesc();
	}

	public Expense findById(Long id) {
		return expenseRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Expense", id));
	}

	@Transactional
	public Expense create(Expense expense) {
		expense.setId(null);
		return expenseRepository.save(expense);
	}

	@Transactional
	public Expense update(Long id, Expense changes) {
		Expense expense = findById(id);
		expense.setTitle(changes.getTitle());
		expense.setAmount(changes.getAmount());
		expense.setCategory(changes.getCategory());
		expense.setDate(changes.getDate());
		return expenseRepository.save(expense);
	}

	@Transactional
	public void delete(Long id) {
		expenseRepository.delete(findById(id));
	}

	public BigDecimal getTotalSpendingByCategory(String category) {
		BigDecimal total = expenseRepository.getTotalSpendingByCategory(category);
		return total == null ? BigDecimal.ZERO : total;
	}

	public BigDecimal getTotalSpending() {
		return expenseRepository.findAll().stream()
				.map(Expense::getAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
	}
}
