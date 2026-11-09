package com.example.account.service;

import com.example.account.dto.AccountCreateRequest;
import com.example.account.exception.BadRequestException;
import com.example.account.exception.NotFoundException;
import com.example.account.model.SavingsAccount;
import com.example.account.repository.SavingsAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavingsAccountServiceTest {

    @Mock
    private SavingsAccountRepository accountRepository;

    @InjectMocks
    private SavingsAccountService savingsAccountService;

    @Test
    void createAccount_FirstAccount_Success() {
        // Arrange
        AccountCreateRequest request = new AccountCreateRequest();
        request.setCustomerName("John Doe");
        request.setAccountNickName("Primary");

        when(accountRepository.findByCustomerNameIgnoreCase("John Doe")).thenReturn(Collections.emptyList());
        when(accountRepository.save(any(SavingsAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        SavingsAccount created = savingsAccountService.createAccount(request);

        // Assert
        assertNotNull(created);
        assertEquals("John Doe", created.getCustomerName());
        assertEquals("Primary", created.getAccountNickName());
        assertNotNull(created.getAccountNumber());

        // Verify Westpac NZ format for the first account (ends with -000)
        assertTrue(created.getAccountNumber().matches("^03-\\d{4}-\\d{7}-000$"));
        verify(accountRepository, times(1)).save(any(SavingsAccount.class));
    }

    @Test
    void createAccount_SubsequentAccount_ReusesBranchAndBaseWithIncrementedSuffix() {
        // Arrange
        AccountCreateRequest request = new AccountCreateRequest();
        request.setCustomerName("John Doe");
        request.setAccountNickName("Vacation");

        String existingAccountNumber = "03-1234-5678901-000";
        SavingsAccount existingAccount = SavingsAccount.builder()
                .id(1L)
                .customerName("John Doe")
                .accountNumber(existingAccountNumber)
                .build();

        // Customer already has 2 accounts (indices 0 and 1)
        List<SavingsAccount> existingAccounts = List.of(
                existingAccount,
                SavingsAccount.builder().id(2L).customerName("John Doe").accountNumber("03-1234-5678901-001").build()
        );

        when(accountRepository.findByCustomerNameIgnoreCase("John Doe")).thenReturn(existingAccounts);
        when(accountRepository.save(any(SavingsAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        SavingsAccount created = savingsAccountService.createAccount(request);

        // Assert
        assertNotNull(created);
        // Should reuse branch (1234) and base account (5678901), but suffix should be 002 (since size is 2)
        assertEquals("03-1234-5678901-002", created.getAccountNumber());

        ArgumentCaptor<SavingsAccount> accountCaptor = ArgumentCaptor.forClass(SavingsAccount.class);
        verify(accountRepository).save(accountCaptor.capture());
        assertEquals("03-1234-5678901-002", accountCaptor.getValue().getAccountNumber());
    }

    @Test
    void createAccount_ExceedsLimit_ThrowsBadRequestException() {
        // Arrange
        AccountCreateRequest request = new AccountCreateRequest();
        request.setCustomerName("Jane Doe");

        // Mock 5 existing accounts
        List<SavingsAccount> maxAccounts = List.of(
                new SavingsAccount(1L, "03-1111-1111111-000", "Jane Doe", "Acc1"),
                new SavingsAccount(2L, "03-1111-1111111-001", "Jane Doe", "Acc2"),
                new SavingsAccount(3L, "03-1111-1111111-002", "Jane Doe", "Acc3"),
                new SavingsAccount(4L, "03-1111-1111111-003", "Jane Doe", "Acc4"),
                new SavingsAccount(5L, "03-1111-1111111-004", "Jane Doe", "Acc5")
        );

        when(accountRepository.findByCustomerNameIgnoreCase("Jane Doe")).thenReturn(maxAccounts);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            savingsAccountService.createAccount(request);
        });

        assertTrue(exception.getMessage().contains("maximum limit of 5 savings accounts"));
        verify(accountRepository, never()).save(any(SavingsAccount.class));
    }

    @Test
    void getAccountByNumber_Success() {
        // Arrange
        String accountNumber = "03-1234-5678901-000";
        SavingsAccount account = SavingsAccount.builder()
                .id(1L)
                .customerName("John Doe")
                .accountNumber(accountNumber)
                .build();

        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.of(account));

        // Act
        SavingsAccount found = savingsAccountService.getAccountByNumber(accountNumber);

        // Assert
        assertNotNull(found);
        assertEquals(accountNumber, found.getAccountNumber());
        assertEquals("John Doe", found.getCustomerName());
    }

    @Test
    void getAccountByNumber_NotFound_ThrowsNotFoundException() {
        // Arrange
        String accountNumber = "03-9999-9999999-999";
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.empty());

        // Act & Assert
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            savingsAccountService.getAccountByNumber(accountNumber);
        });

        assertTrue(exception.getMessage().contains("Savings account not found"));
    }

    @Test
    void getAccountCustomerName_Success() {
        // Arrange
        String customerName = "John Doe";
        List<SavingsAccount> accounts = List.of(
                SavingsAccount.builder().id(1L).customerName(customerName).accountNumber("03-1234-5678901-000").build()
        );

        when(accountRepository.findByCustomerNameIgnoreCase(customerName)).thenReturn(accounts);

        // Act
        List<SavingsAccount> result = savingsAccountService.getAccountCustomerName(customerName);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(customerName, result.get(0).getCustomerName());
    }
}
