package com.skytech.projectmanagement.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordValidator implements ConstraintValidator<Password, String> {

    // Regex: ít nhất 8 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt
    private static final String PASSWORD_PATTERN =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&#^()_+\\-=\\[\\]{};':\",./<>?|\\\\~`])[A-Za-z\\d@$!%*?&#^()_+\\-=\\[\\]{};':\",./<>?|\\\\~`]{8,}$";

    @Override
    public void initialize(Password constraintAnnotation) {
        // No initialization needed
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank()) {
            return false;
        }

        boolean isValid = password.matches(PASSWORD_PATTERN);

        if (!isValid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(
                    "Mật khẩu phải có ít nhất 8 ký tự, bao gồm chữ hoa, chữ thường, số và ký tự đặc biệt")
                    .addConstraintViolation();
        }

        return isValid;
    }
}

