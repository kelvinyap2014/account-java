package com.example.account.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "savings_accounts", indexes = {
        @Index(name = "idx_customer_name", columnList = "customerName")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavingsAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // (CC-BBBB-AAAAAAA-SSS)
    @Column(nullable = false, unique = true, updatable = false, length = 20)
    private String accountNumber;

    @Column(nullable = false, updatable = false, length = 100)
    private String customerName;

    @Column(length = 30)
    private String accountNickName;
}
