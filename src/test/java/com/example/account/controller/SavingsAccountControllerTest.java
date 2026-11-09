package com.example.account.controller;

import com.example.account.dto.AccountCreateRequest;
import com.example.account.exception.BadRequestException;
import com.example.account.exception.NotFoundException;
import com.example.account.model.SavingsAccount;
import com.example.account.service.SavingsAccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SavingsAccountController.class)
class SavingsAccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SavingsAccountService savingsAccountService;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createAccount_Success() throws Exception {
        AccountCreateRequest request = new AccountCreateRequest();
        request.setCustomerName("John Doe");
        request.setAccountNickName("MySavings");

        SavingsAccount createdAccount = SavingsAccount.builder()
                .id(1L)
                .customerName("John Doe")
                .accountNickName("MySavings")
                .accountNumber("03-1234-5678901-000")
                .build();

        when(savingsAccountService.createAccount(any(AccountCreateRequest.class))).thenReturn(createdAccount);

        mockMvc.perform(post("/api/v1/savings-accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.customerName").value("John Doe"))
                .andExpect(jsonPath("$.accountNickName").value("MySavings"))
                .andExpect(jsonPath("$.accountNumber").value("03-1234-5678901-000"));
    }

    @Test
    void createAccount_ValidationFailure_BlankCustomerName() throws Exception {
        AccountCreateRequest request = new AccountCreateRequest();
        request.setCustomerName(""); // Invalid: mandatory

        mockMvc.perform(post("/api/v1/savings-accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createAccount_BadRequestException_MaxLimitReached() throws Exception {
        AccountCreateRequest request = new AccountCreateRequest();
        request.setCustomerName("Jane Doe");
        String message = "Customer 'Jane Doe' has already reached the maximum limit of 5 savings accounts.";

        when(savingsAccountService.createAccount(any(AccountCreateRequest.class)))
                .thenThrow(new BadRequestException(message));

        mockMvc.perform(post("/api/v1/savings-accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(message));
    }

    @Test
    void getAccountByNumber_Success() throws Exception {
        String accountNumber = "03-1234-5678901-000";
        SavingsAccount account = SavingsAccount.builder()
                .id(1L)
                .customerName("John Doe")
                .accountNumber(accountNumber)
                .build();

        when(savingsAccountService.getAccountByNumber(eq(accountNumber))).thenReturn(account);

        mockMvc.perform(get("/api/v1/savings-accounts/{accountNumber}", accountNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value(accountNumber))
                .andExpect(jsonPath("$.customerName").value("John Doe"));
    }

    @Test
    void getAccountByNumber_NotFound() throws Exception {
        String accountNumber = "03-9999-9999999-999";
        String message = "Savings account not found with account number: " + accountNumber;

        when(savingsAccountService.getAccountByNumber(eq(accountNumber)))
                .thenThrow(new NotFoundException(message));

        mockMvc.perform(get("/api/v1/savings-accounts/{accountNumber}", accountNumber))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(message));
    }

    @Test
    void getAccountsByCustomerName_Success() throws Exception {
        String customerName = "John Doe";
        List<SavingsAccount> accounts = List.of(
                SavingsAccount.builder().id(1L).customerName(customerName).accountNumber("03-1234-5678901-000").build(),
                SavingsAccount.builder().id(2L).customerName(customerName).accountNumber("03-1234-5678901-001").build()
        );

        when(savingsAccountService.getAccountCustomerName(eq(customerName))).thenReturn(accounts);

        mockMvc.perform(get("/api/v1/savings-accounts/customer/{customerName}", customerName))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].accountNumber").value("03-1234-5678901-000"))
                .andExpect(jsonPath("$[1].accountNumber").value("03-1234-5678901-001"));
    }
}
