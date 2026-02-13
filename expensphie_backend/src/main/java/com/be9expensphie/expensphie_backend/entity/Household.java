package com.be9expensphie.expensphie_backend.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name = "household")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class Household {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique=true,nullable=false)
    private String name;
    
    @Column(unique=true,nullable=false)
    private String code;
    
    @OneToMany(mappedBy="household",cascade=CascadeType.ALL,orphanRemoval=true)
    private List<HouseholdMember> members;
    
    //regarding design aspect this can achieve through intermediate entity
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "created_by",
        referencedColumnName = "id",
        nullable = false
    )
    private UserEntity createdBy;
    
    @OneToMany(mappedBy = "household")
    @Builder.Default
    private List<ExpenseEntity> expenses = new ArrayList<>();
   
}
