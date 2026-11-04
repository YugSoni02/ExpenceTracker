package com.example.expensetracker.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "expenses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotBlank
	@Size(max = 120)
	@Column(nullable = false, length = 120)
	private String title;

	@NotNull
	@DecimalMin("0.01")
	@Digits(integer = 10, fraction = 2)
	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal amount;

	@NotBlank
	@Size(max = 60)
	@Column(nullable = false, length = 60)
	private String category;

	@NotNull
	@Column(nullable = false)
	private LocalDate date;
}
