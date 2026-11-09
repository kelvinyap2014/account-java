package com.example.account.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = NickNameValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidNickName {
    String message() default "Invalid nickname: must be between 5 and 30 characters and cannot contain offensive language.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
