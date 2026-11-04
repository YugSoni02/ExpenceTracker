package com.example.expensetracker.controller.api;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.service.ExpenseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Validated
public class ExpenseRestController {

	private final ExpenseService expenseService;

	@GetMapping
	public List<Expense> findAll() {
		return expenseService.findAll();
	}

	@GetMapping("/{id}")
	public Expense findById(@PathVariable Long id) {
		return expenseService.findById(id);
	}

	@PostMapping
	public ResponseEntity<Expense> create(@Valid @RequestBody Expense expense) {
		Expense created = expenseService.create(expense);
		URI location = ServletUriComponentsBuilder.fromCurrentRequest()
				.path("/{id}")
				.buildAndExpand(created.getId())
				.toUri();
		return ResponseEntity.created(location).body(created);
	}

	@PutMapping("/{id}")
	public Expense update(@PathVariable Long id, @Valid @RequestBody Expense expense) {
		return expenseService.update(id, expense);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		expenseService.delete(id);
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/category/{category}/total")
	public CategoryTotal getTotalByCategory(@PathVariable String category) {
		return new CategoryTotal(category, expenseService.getTotalSpendingByCategory(category));
	}

	public record CategoryTotal(String category, BigDecimal total) {
	}
}
