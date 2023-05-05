package dv.kinash.hw15.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserNewDTO {
    @NotBlank
    private String name;
    @Email
    private String email;
    @NotBlank
    private String password;
}
