package com.example.account.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;

public class NickNameValidator implements ConstraintValidator<ValidNickName, String> {

    // TODO: In production, load this list securely from an external configuration file, database, or API
    private static final List<String> OFFENSIVE_WORDS = List.of("badword", "offensive", "spam", "scam");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Since account nickname is optional, null or blank values are considered valid
        if (value == null || value.isBlank()) {
            return true;
        }

        String trimmed = value.trim();

        // Rule: Nickname must be between 5 and 30 characters
        if (trimmed.length() < 5 || trimmed.length() > 30) {
            return false;
        }

        // Rule: Check against offensive words list
        String lowerCaseValue = trimmed.toLowerCase();
        // Returns true if NONE of the offensive words are contained in the nickname
        return OFFENSIVE_WORDS.stream().noneMatch(lowerCaseValue::contains);
    }
}
