package com.be9expensphie.expensphie_backend.entity;

import com.be9expensphie.expensphie_backend.enums.HouseholdRole;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "household_members",
		uniqueConstraints=@UniqueConstraint(
				columnNames= {"household_id","user_id"}
				)
)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter

public class HouseholdMember {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable=false)
	private HouseholdRole role;
	
	@ManyToOne
	@JoinColumn(name="user_id")
	private UserEntity user;
	
	@ManyToOne
	@JoinColumn(name="household_id")
	private Household household;
	 

}
