package com.be9expensphie.expensphie_backend.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.be9expensphie.expensphie_backend.enums.ExpenseStatus;
import com.be9expensphie.expensphie_backend.enums.Method;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "expense")
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