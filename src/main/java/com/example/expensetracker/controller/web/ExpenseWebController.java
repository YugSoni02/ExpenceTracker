package com.example.expensetracker.controller.web;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.example.expensetracker.exception.ResourceNotFoundException;
import com.example.expensetracker.model.Expense;
import com.example.expensetracker.service.ExpenseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/expenses")
@RequiredArgsConstructor
public class ExpenseWebController {

	private static final List<String> SUGGESTED_CATEGORIES = List.of(
			"Food & dining", "Transport", "Shopping", "Housing", "Health", "Entertainment", "Travel", "Other");

	private final ExpenseService expenseService;

	@GetMapping({ "", "/" })
	public String dashboard(Model model) {
		List<Expense> expenses = expenseService.findAll();
		Map<String, BigDecimal> categoryTotals = new LinkedHashMap<>();
		expenses.stream()
				.map(Expense::getCategory)
				.distinct()
				.forEach(category -> categoryTotals.put(category,
						expenseService.getTotalSpendingByCategory(category)));

		model.addAttribute("expenses", expenses.stream().limit(6).toList());
		model.addAttribute("total", expenseService.getTotalSpending());
		model.addAttribute("expenseCount", expenses.size());
		BigDecimal maxCategoryTotal = categoryTotals.values().stream()
				.max(BigDecimal::compareTo)
				.orElse(BigDecimal.ZERO);
		List<CategorySummary> categories = categoryTotals.entrySet().stream()
				.map(entry -> new CategorySummary(entry.getKey(), entry.getValue(),
						maxCategoryTotal.signum() == 0 ? 0
								: entry.getValue().multiply(BigDecimal.valueOf(100))
										.divide(maxCategoryTotal, 0, RoundingMode.HALF_UP).intValue()))
				.toList();
		model.addAttribute("categories", categories);
		return "index";
	}

	@GetMapping("/new")
	public String newExpense(Model model) {
		model.addAttribute("expense", Expense.builder().date(java.time.LocalDate.now()).build());
		model.addAttribute("formTitle", "Add an expense");
		model.addAttribute("formAction", "/expenses");
		addCategories(model);
		return "form";
	}

	@PostMapping
	public String createExpense(@Valid @ModelAttribute("expense") Expense expense,
			BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("formTitle", "Add an expense");
			model.addAttribute("formAction", "/expenses");
			addCategories(model);
			return "form";
		}
		expenseService.create(expense);
		redirectAttributes.addFlashAttribute("successMessage", "Expense added successfully.");
		return "redirect:/expenses";
	}

	@GetMapping("/{id}")
	public String details(@PathVariable Long id, Model model) {
		model.addAttribute("expense", expenseService.findById(id));
		return "details";
	}

	@GetMapping("/{id}/edit")
	public String editExpense(@PathVariable Long id, Model model) {
		model.addAttribute("expense", expenseService.findById(id));
		model.addAttribute("formTitle", "Edit expense");
		model.addAttribute("formAction", "/expenses/" + id + "/edit");
		addCategories(model);
		return "form";
	}

	@PostMapping("/{id}/edit")
	public String updateExpense(@PathVariable Long id, @Valid @ModelAttribute("expense") Expense expense,
			BindingResult bindingResult, Model model, RedirectAttributes redirectAttributes) {
		expenseService.findById(id);
		if (bindingResult.hasErrors()) {
			model.addAttribute("formTitle", "Edit expense");
			model.addAttribute("formAction", "/expenses/" + id + "/edit");
			addCategories(model);
			return "form";
		}
		expenseService.update(id, expense);
		redirectAttributes.addFlashAttribute("successMessage", "Your changes have been saved.");
		return "redirect:/expenses/" + id;
	}

	@PostMapping("/{id}/delete")
	public String deleteExpense(@PathVariable Long id, RedirectAttributes redirectAttributes) {
		expenseService.delete(id);
		redirectAttributes.addFlashAttribute("successMessage", "Expense deleted.");
		return "redirect:/expenses";
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String handleMissingExpense() {
		return "error/404";
	}

	private void addCategories(Model model) {
		model.addAttribute("suggestedCategories", SUGGESTED_CATEGORIES);
	}

	public record CategorySummary(String name, BigDecimal amount, int percentage) {
	}
}
