package com.be9expensphie.expensphie_backend.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.be9expensphie.expensphie_backend.enums.SettlementStatus;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "settlements", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "from_member_id", "to_member_id", "expense_split_details_id" })
}, indexes={
        @Index(name="idx_dashboard_query",columnList ="from_member_id,status,date"),
        @Index(name="idx_settlement_to_member_status",columnList ="to_member_id,status"),
        @Index(name="idx_settlement_split_details",columnList ="expense_split_details_id"),
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SettlementEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "from_member_id", nullable = false)
    private HouseholdMember fromMember;
    @ManyToOne
    @JoinColumn(name = "to_member_id", nullable = false)
    private HouseholdMember toMember;
    @OneToOne
    @JoinColumn(name = "expense_split_details_id", nullable = false)
    private ExpenseSplitDetailsEntity expenseSplitDetails;
    @Column(nullable = false)
    private BigDecimal amount;
    @Column(nullable = false)
    private String currency;
    @Column(nullable = false)
    private LocalDate date;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SettlementStatus status;

    @PrePersist
    public void prePersist() {
        if (date == null) {
            date = LocalDate.now();
        }
        if (status == null) {
            status = SettlementStatus.PENDING;
        }
    }
}
