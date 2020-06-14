package me.danieluss.tournaments.data.dto;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class RegisterUser {

    @NotBlank(message = "Must be a valid email.")
    @Email(message = "Must be a valid email.")
    private String email;

    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*_+\\-=,.:;])(?=\\S+$).{8,16}$", message = "Incorrect\n" +
            "            password (at least 1 special character [!@#$%^&*_+-=,.:;], at least 1 number, at least 1 capital and lower-case letter, 8 to 16 characters)")
    private String password;
    private String confirmPassword;

    @NotBlank(message = "Must not be blank.")
    private String firstName;

    @NotBlank(message = "Must not be blank.")
    private String lastName;


}
