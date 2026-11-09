package com.example.account.dto;

import com.example.account.validation.ValidNickName;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AccountCreateRequest {

    @NotBlank(message = "Customer name is mandatory.")
    @Size(min = 2, max = 100, message = "Customer name must be between 2 and 100 characters.")
    private String customerName;

    @ValidNickName
    private String accountNickName;
}
