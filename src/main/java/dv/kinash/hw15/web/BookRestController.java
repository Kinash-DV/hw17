package dv.kinash.hw15.web;

import dv.kinash.hw15.dto.BookInfoDTO;
import dv.kinash.hw15.dto.BookInfoWithReaderDTO;
import dv.kinash.hw15.dto.BookNewDTO;
import dv.kinash.hw15.service.BookService;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@SecurityScheme(name = "jwt",type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class BookRestController {
    private final BookService bookService;

    @GetMapping("/admin/all")
    @SecurityRequirement(name = "jwt")
    public List<BookInfoWithReaderDTO> getAll(){
        return bookService.getAllBook();
    }
    @PostMapping("/admin/add")
    @SecurityRequirement(name = "jwt")
    @ResponseStatus(HttpStatus.CREATED)
    public BookInfoDTO addNewBook(@Valid @RequestBody BookNewDTO newBook){
        return bookService.addGetNewBook(newBook);
    }
    @DeleteMapping("/admin/{id}")
    @SecurityRequirement(name = "jwt")
    public void deleteBook(@PathVariable Long id){
        bookService.deleteBook(id);
    }
    @GetMapping("/get_available")
    @SecurityRequirement(name = "jwt")
    public List<BookInfoDTO> getAvailable(Principal principal){
        return bookService.getAvailableBook(principal.getName());
    }
    @GetMapping("/my")
    @SecurityRequirement(name = "jwt")
    public List<BookInfoDTO> getMyBook(Principal principal){
        return bookService.getUserBook(principal.getName());
    }
    @GetMapping("/take/{id}")
    @SecurityRequirement(name = "jwt")
    public void takeBook(@PathVariable Long id, Principal principal){
        bookService.takeBook(principal.getName(), id);
    }
    @GetMapping("/return/{id}")
    @SecurityRequirement(name = "jwt")
    public void returnBook(@PathVariable Long id, Principal principal){
        bookService.returnBook(principal.getName(), id);
    }
}
