package com.example.account.controller;

import com.example.account.dto.AccountCreateRequest;
import com.example.account.model.SavingsAccount;
import com.example.account.service.SavingsAccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/savings-accounts")
@RequiredArgsConstructor
public class SavingsAccountController {

    private final SavingsAccountService accountService;

    @PostMapping
    public ResponseEntity<SavingsAccount> createAccount(@Valid @RequestBody AccountCreateRequest request) {
        SavingsAccount createdAccount = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<SavingsAccount> getAccountByNumber(@PathVariable String accountNumber) {
        SavingsAccount account = accountService.getAccountByNumber(accountNumber);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/customer/{customerName}")
    public ResponseEntity<List<SavingsAccount>> getAccountsByCustomerName(@PathVariable String customerName) {
        List<SavingsAccount> accounts = accountService.getAccountCustomerName(customerName);
        return ResponseEntity.ok(accounts);
    }
}
