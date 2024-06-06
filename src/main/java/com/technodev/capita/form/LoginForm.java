package com.technodev.capita.form;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginForm {
    @NotEmpty
    private String email;
    @NotEmpty
    private String password;
}
