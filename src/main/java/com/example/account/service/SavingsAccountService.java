package com.example.account.service;

import com.example.account.dto.AccountCreateRequest;
import com.example.account.exception.BadRequestException;
import com.example.account.exception.NotFoundException;
import com.example.account.model.SavingsAccount;
import com.example.account.repository.SavingsAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class SavingsAccountService {

    private final SavingsAccountRepository accountRepository;
    private final Random random = new Random();

    @Transactional
    public SavingsAccount createAccount(AccountCreateRequest request) {
        List<SavingsAccount> existingAccounts = accountRepository.findByCustomerNameIgnoreCase(request.getCustomerName());

        // Business Rule: Maximum 5 accounts per customer
        if (existingAccounts.size() >= 5) {
            throw new BadRequestException("Customer '" + request.getCustomerName() + "' has already reached the maximum limit of 5 savings accounts.");
        }

        String branchCode;
        String baseAccount;
        String suffix;

        if (existingAccounts.isEmpty()) {
            // First account: Generate new branch code and non-zero base account (0000001 to 9999999)
            branchCode = String.format("%04d", random.nextInt(1999) + 1);
            baseAccount = String.format("%07d", random.nextInt(9999999) + 1);
            suffix = "000";
        } else {
            // Subsequent accounts: Reuse branch code and base account from existing accounts
            String existingAccountNumber = existingAccounts.get(0).getAccountNumber();
            String[] parts = existingAccountNumber.split("-");
            branchCode = parts[1];
            baseAccount = parts[2];

            // Increment suffix based on how many accounts currently exist (0 -> "000", 1 -> "001", etc.)
            suffix = String.format("%03d", existingAccounts.size());
        }

        String accountNumber = String.format("03-%s-%s-%s", branchCode, baseAccount, suffix);

        SavingsAccount account = SavingsAccount.builder()
                .customerName(request.getCustomerName())
                .accountNickName(request.getAccountNickName())
                .accountNumber(accountNumber)
                .build();

        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public SavingsAccount getAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new NotFoundException("Savings account not found with account number: " + accountNumber));
    }

    @Transactional(readOnly = true)
    public List<SavingsAccount> getAccountCustomerName(String customerName) {
        List<SavingsAccount> accounts = accountRepository.findByCustomerNameIgnoreCase(customerName);
        if (Objects.isNull(accounts) || accounts.isEmpty()) {
            throw new NotFoundException("Savings account not found with customer: " + customerName);
        }
        return accounts;
    }
}
