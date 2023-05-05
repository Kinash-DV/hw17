package dv.kinash.hw15.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dv.kinash.hw15.dto.BookInfoDTO;
import dv.kinash.hw15.dto.BookInfoWithReaderDTO;
import dv.kinash.hw15.dto.BookNewDTO;
import dv.kinash.hw15.dto.LoginDTO;
import dv.kinash.hw15.repository.BookRepository;
import dv.kinash.hw15.repository.UserRepository;
import dv.kinash.hw15.repository.entity.Book;
import dv.kinash.hw15.repository.entity.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BookRestControllerIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    BookRepository bookRepository;

    static User admin;
    static User reader;
    static User anotherReader;

    @BeforeAll
    static void prepareUsers(
            @Autowired UserRepository userRepository,
            @Autowired PasswordEncoder passwordEncoder) {

        userRepository.deleteAll();

        admin = new User(null, "Admin", "admin@book.com", passwordEncoder.encode("a#123"), true);
        admin = userRepository.save(admin);
        admin.setPassword("a#123");

        reader = new User(null,"Reader", "reader@book.com", passwordEncoder.encode("r#123"), false);
        reader = userRepository.save(reader);
        reader.setPassword("r#123");

        anotherReader = new User(null,"Another reader", "areader@book.com", passwordEncoder.encode("ar#123"), false);
        anotherReader = userRepository.save(anotherReader);
        anotherReader.setPassword("ar#123");
    }

    @AfterAll
    static void prepareUsers(@Autowired BookRepository bookRepository) {
        bookRepository.deleteAll();
    }

    @BeforeEach
    void prepareBooks() {
        List<Book> newBooks = Arrays.asList(
                new Book(1L, "The Five Orange Pips", "Arthur Conan Doyle", 1860920314L, null),
                new Book(2L, "The Ransom of Red Chief; Gift of the Magi", "O Henry", 186092011L, reader),
                new Book(3L, "The Five Orange Pips", "Arthur Conan Doyle", 1860920314L, null)
        );
        bookRepository.deleteAll();
        bookRepository.saveAll(newBooks);
    }

    private String getLoginToken(String login, String password) throws Exception {
        var loginDTO = new LoginDTO(login, password);
        var result = mvc.perform(post("/api/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn();

        var token = result.getResponse().getContentAsString();
        Assertions.assertNotEquals("", token);

        return token;
    }

    @Test
    void getAllTest() throws Exception {
        String adminToken = getLoginToken(admin.getEmail(), admin.getPassword());
        String readerToken = getLoginToken(reader.getEmail(), reader.getPassword());

        mvc.perform(get("/api/books/admin/all")
                        .header("Authorization", "Bearer " + readerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        var result = mvc.perform(get("/api/books/admin/all")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        List<BookInfoWithReaderDTO> bookList = new ObjectMapper().readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<List<BookInfoWithReaderDTO>>(){});
        Assertions.assertEquals(bookRepository.count(), bookList.size());

        BookNewDTO newBook = new BookNewDTO("", "", 999999999L);

    }

    @Test
    void addNewBookTest() throws Exception {
        String adminToken = getLoginToken(admin.getEmail(), admin.getPassword());
        String readerToken = getLoginToken(reader.getEmail(), reader.getPassword());

        BookNewDTO newBook = new BookNewDTO(
                "The Lion, the Witch and the Wardrobe", "C. S. Lewis", 9780064404990L);

        mvc.perform(post("/api/books/admin/add")
                        .header("Authorization", "Bearer " + readerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(newBook)))
                .andExpect(status().isForbidden());

        var wrongBook1 = new BookNewDTO(newBook.getDescription(), newBook.getAuthor(), 999999999L);
        var wrongBook2 = new BookNewDTO(newBook.getDescription(), newBook.getAuthor(), 10000000000000L);
        var wrongBook3 = new BookNewDTO(newBook.getDescription(), "", newBook.getISBN());
        var wrongBook4 = new BookNewDTO("", newBook.getAuthor(), newBook.getISBN());

        mvc.perform(post("/api/books/admin/add")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(wrongBook1)))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/books/admin/add")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(wrongBook2)))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/books/admin/add")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(wrongBook3)))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/books/admin/add")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(wrongBook4)))
                .andExpect(status().isBadRequest());

        var result = mvc.perform(post("/api/books/admin/add")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(newBook)))
                .andExpect(status().isCreated())
                .andReturn();

        BookInfoDTO bookInfoDTO = new ObjectMapper().readValue(
                result.getResponse().getContentAsString(),
                BookInfoDTO.class);
        Assertions.assertNotNull(bookInfoDTO);
        Assertions.assertNotNull(bookInfoDTO.getId());

        Book bookFromDB = bookRepository.findById(bookInfoDTO.getId()).get();
        Assertions.assertNotNull(bookFromDB);

    }

    @Test
    void deleteBookTest() throws Exception {
        var oldBookCount = bookRepository.count();
        var newBook = new Book(
                null,
                "The Lion, the Witch and the Wardrobe",
                "C. S. Lewis",
                9780064404990L,
                null);
        newBook = bookRepository.save(newBook);
        Assertions.assertEquals(oldBookCount + 1, bookRepository.count());
        var idToDelete = newBook.getId();

        String adminToken = getLoginToken(admin.getEmail(), admin.getPassword());
        String readerToken = getLoginToken(reader.getEmail(), reader.getPassword());

        mvc.perform(delete("/api/books/admin/" + idToDelete)
                        .header("Authorization", "Bearer " + readerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());

        mvc.perform(delete("/api/books/admin/" + idToDelete)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        Assertions.assertEquals(oldBookCount, bookRepository.count());

        mvc.perform(delete("/api/books/admin/" + idToDelete)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void takeAndReturnBookTest() throws Exception {
        String readerToken = getLoginToken(reader.getEmail(), reader.getPassword());
        String anotherToken = getLoginToken(anotherReader.getEmail(), anotherReader.getPassword());

        // Now: I took one book and left two in the library
        var result = mvc.perform(get("/api/books/my")
                        .header("Authorization", "Bearer " + readerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        List<BookInfoDTO> myBookList = new ObjectMapper().readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<List<BookInfoDTO>>(){});
        Assertions.assertEquals(1, myBookList.size());
        Long myBookId = myBookList.get(0).getId();

        result = mvc.perform(get("/api/books/get_available")
                        .header("Authorization", "Bearer " + readerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        List<BookInfoDTO> availableBookList = new ObjectMapper().readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<List<BookInfoDTO>>(){});
        Assertions.assertEquals(2, availableBookList.size());

        // error checking
        mvc.perform(get("/api/books/take/" + 123456L)
                        .header("Authorization", "Bearer " + readerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/books/return/" + 123456L)
                        .header("Authorization", "Bearer " + readerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        mvc.perform(get("/api/books/return/" + availableBookList.get(0).getId())
                        .header("Authorization", "Bearer " + readerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/books/return/" + myBookId)
                        .header("Authorization", "Bearer " + anotherToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        // Now: I return the book
        mvc.perform(get("/api/books/return/" + myBookId)
                        .header("Authorization", "Bearer " + readerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        result = mvc.perform(get("/api/books/my")
                        .header("Authorization", "Bearer " + readerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        myBookList = new ObjectMapper().readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<List<BookInfoDTO>>(){});
        Assertions.assertEquals(0, myBookList.size());

        result = mvc.perform(get("/api/books/get_available")
                        .header("Authorization", "Bearer " + readerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        availableBookList = new ObjectMapper().readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<List<BookInfoDTO>>(){});
        Assertions.assertEquals(3, availableBookList.size());

        // Now: I take the book again
        mvc.perform(get("/api/books/take/" + myBookId)
                        .header("Authorization", "Bearer " + readerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        result = mvc.perform(get("/api/books/my")
                        .header("Authorization", "Bearer " + readerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        myBookList = new ObjectMapper().readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<List<BookInfoDTO>>(){});
        Assertions.assertEquals(1, myBookList.size());

        result = mvc.perform(get("/api/books/get_available")
                        .header("Authorization", "Bearer " + readerToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        availableBookList = new ObjectMapper().readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<List<BookInfoDTO>>(){});
        Assertions.assertEquals(2, availableBookList.size());

    }

}