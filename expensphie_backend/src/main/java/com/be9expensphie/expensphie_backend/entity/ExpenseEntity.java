package com.be9expensphie.expensphie_backend.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.be9expensphie.expensphie_backend.enums.ExpenseStatus;
import com.be9expensphie.expensphie_backend.enums.Method;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "expense",
	indexes = {
			@Index(name="idx_created_by_id",columnList ="created_by_id"),
			@Index(name="idx_expense_list",columnList ="household_id,status,id"),
			@Index(name="idx_expense_date_range",columnList ="household_id,status,date"),
			@Index(name="idx_reviewed_by_id",columnList ="reviewed_by_id"),
	})
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class ExpenseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(nullable=false)
	private BigDecimal amount;
	@Column(nullable=false)
	private String currency;
	@Column(nullable=false)
	private LocalDate date;
	@Column(nullable=false)
	private String category;
	@Enumerated(EnumType.STRING)
	@Column(nullable=false)
	private ExpenseStatus status;
	@Column(nullable = true)
	private String description;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable=false)
	private Method method;
	
	//who create expense, 1 member can create many
	@ManyToOne
	@JoinColumn(name="created_by_id",nullable=false)
	private HouseholdMember created_by;
	
	//admin
	@ManyToOne
	@JoinColumn(name="reviewed_by_id",nullable=false)
	private HouseholdMember reviewed_by;
	
    @OneToMany(
            mappedBy = "expense",
            cascade = CascadeType.ALL,
            orphanRemoval = true
        )
	@Builder.Default
	private List<ExpenseSplitDetailsEntity> splitDetails=new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "household_id")
    private Household household;

}