package com.be9expensphie.expensphie_backend.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "expense_split_details")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
	
public class ExpenseSplitDetailsEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	//many splits belong to 1 expense
	@ManyToOne
	@JoinColumn(nullable=false,name="expense_id")
	private ExpenseEntity expense;
	
	@Column(nullable=false)
	private BigDecimal amount;
	
	//many split belong to 1 member
	@ManyToOne
	@JoinColumn(nullable=false,name="member_id")
	private HouseholdMember member;
}
