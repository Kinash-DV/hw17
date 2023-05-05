package dv.kinash.hw15.web;

import dv.kinash.hw15.dto.UserDTO;
import dv.kinash.hw15.dto.UserNewDTO;
import dv.kinash.hw15.service.UserService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@SecurityScheme(name = "jwt",type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class UserRestController {

    private final UserService userService;

    @PostMapping("/admin")
    @SecurityRequirement(name = "jwt")
    @ResponseStatus(HttpStatus.CREATED)
    public void addNewAdmin(@Valid @RequestBody UserNewDTO newUser){
        userService.addUser(newUser, true);
    }
    @PostMapping("/reader")
    @SecurityRequirement(name = "jwt")
    @ResponseStatus(HttpStatus.CREATED)
    public void addNewReader(@Valid @RequestBody UserNewDTO newUser){
        userService.addUser(newUser, false);
    }

    @GetMapping("/list")
    @SecurityRequirement(name = "jwt")
    public List<UserDTO> getUsers(){
        return userService.getUsers();
    }


}
