package com.example.expensetracker;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.expensetracker.model.Expense;
import com.example.expensetracker.repository.ExpenseRepository;

@SpringBootTest
@AutoConfigureMockMvc
class ExpenseTrackerApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ExpenseRepository expenseRepository;

	@BeforeEach
	void clearExpenses() {
		expenseRepository.deleteAll();
	}

	@Test
	void dashboardRequiresAuthentication() throws Exception {
		mockMvc.perform(get("/expenses"))
				.andExpect(status().is3xxRedirection())
				.andExpect(header().string("Location", containsString("/login")));
	}

	@Test
	void authenticatedDashboardRenders() throws Exception {
		mockMvc.perform(get("/expenses").with(user("expense")))
				.andExpect(status().isOk())
				.andExpect(view().name("index"))
				.andExpect(content().string(containsString("A little clarity")));
	}

	@Test
	void apiCreatesExpensesAndAggregatesCategorySpending() throws Exception {
		mockMvc.perform(post("/api/expenses")
						.with(user("expense"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title":"Market","amount":42.50,"category":"Food","date":"2026-11-04"}
								"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("Market"))
				.andExpect(header().exists("Location"));

		mockMvc.perform(get("/api/expenses/category/Food/total").with(user("expense")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.category").value("Food"))
				.andExpect(jsonPath("$.total").value(42.50));
	}

	@Test
	void apiRejectsInvalidExpenses() throws Exception {
		mockMvc.perform(post("/api/expenses")
						.with(user("expense"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"title":"","amount":0,"category":"","date":null}
								"""))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").isNotEmpty());
	}

	@Test
	void webFormsCreateUpdateShowAndDeleteAnExpense() throws Exception {
		mockMvc.perform(get("/expenses/new").with(user("expense")))
				.andExpect(status().isOk())
				.andExpect(view().name("form"))
				.andExpect(content().string(containsString("Add an expense")));

		mockMvc.perform(post("/expenses")
						.with(user("expense"))
						.param("title", "Morning coffee")
						.param("amount", "4.25")
						.param("category", "Food")
						.param("date", "2026-11-04"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/expenses"));

		Expense created = expenseRepository.findAll().getFirst();
		mockMvc.perform(get("/expenses/{id}", created.getId()).with(user("expense")))
				.andExpect(status().isOk())
				.andExpect(view().name("details"))
				.andExpect(content().string(containsString("Morning coffee")));

		mockMvc.perform(post("/expenses/{id}/edit", created.getId())
						.with(user("expense"))
						.param("title", "Afternoon coffee")
						.param("amount", "5.50")
						.param("category", "Food")
						.param("date", "2026-11-04"))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/expenses/" + created.getId()));

		mockMvc.perform(get("/expenses/{id}", created.getId()).with(user("expense")))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Afternoon coffee")));

		mockMvc.perform(post("/expenses/{id}/delete", created.getId()).with(user("expense")))
				.andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("/expenses"));
	}

	@Test
	void missingWebExpenseShowsNotFoundPage() throws Exception {
		mockMvc.perform(get("/expenses/987654").with(user("expense")))
				.andExpect(status().isNotFound())
				.andExpect(view().name("error/404"));
	}
}
