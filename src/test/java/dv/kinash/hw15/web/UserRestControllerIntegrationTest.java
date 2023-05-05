package dv.kinash.hw15.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dv.kinash.hw15.dto.LoginDTO;
import dv.kinash.hw15.dto.UserDTO;
import dv.kinash.hw15.dto.UserNewDTO;
import dv.kinash.hw15.repository.UserRepository;
import dv.kinash.hw15.repository.entity.User;
import dv.kinash.hw15.service.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserRestControllerIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    UserRepository repository;
    static User admin;

    @BeforeAll
    static void prepareUsers(
            @Autowired UserRepository userRepository,
            @Autowired PasswordEncoder passwordEncoder) {

        userRepository.deleteAll();

        admin = new User(null, "Admin", "admin@book.com", passwordEncoder.encode("a#123"), true);
        admin = userRepository.save(admin);
        admin.setPassword("a#123");

    }

    private String getAdminToken() throws Exception {
        var loginDTO = new LoginDTO(admin.getEmail(), admin.getPassword());
        MvcResult result = mvc.perform(post("/api/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andReturn();
        return result.getResponse().getContentAsString();
    }

    @Test
    void authenticateTest() throws Exception {
        Assertions.assertNotEquals("", getAdminToken());

        var wrongLogin = new LoginDTO("wrong@book.com", "");
        mvc.perform(post("/api/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(wrongLogin)))
                .andExpect(status().isNotFound());

        wrongLogin.setUsername(admin.getEmail());
        mvc.perform(post("/api/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(wrongLogin)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addNewAdmin() throws Exception {

        var adminToken = getAdminToken();
        var newAdmin = new UserNewDTO("New admin", "admin2@book.com", "aB@123");
        mvc.perform(post("/api/users/admin")
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(new ObjectMapper().writeValueAsString(newAdmin)))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));

        var userByEmail = repository.findByEmail(newAdmin.getEmail()).get();
        Assertions.assertNotNull(userByEmail);

        Assertions.assertEquals(newAdmin.getName(), userByEmail.getName());
        Assertions.assertEquals(newAdmin.getEmail(), userByEmail.getEmail());
        Assertions.assertNotEquals(newAdmin.getPassword(), userByEmail.getPassword());
        Assertions.assertTrue(userByEmail.getIsAdmin());

        mvc.perform(post("/api/users/admin")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(newAdmin)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addNewReader() throws Exception {

        var adminToken = getAdminToken();
        var newReader = new UserNewDTO("New reader", "reader2@book.com", "#%354@#");
        mvc.perform(post("/api/users/reader")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(newReader)))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));

        var userByEmail = repository.findByEmail(newReader.getEmail()).get();
        Assertions.assertNotNull(userByEmail);

        Assertions.assertEquals(newReader.getName(), userByEmail.getName());
        Assertions.assertEquals(newReader.getEmail(), userByEmail.getEmail());
        Assertions.assertNotEquals(newReader.getPassword(), userByEmail.getPassword());
        Assertions.assertFalse(userByEmail.getIsAdmin());

        mvc.perform(post("/api/users/reader")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(new ObjectMapper().writeValueAsString(newReader)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUsers() throws Exception {

        MvcResult result = mvc.perform(get("/api/users/list")
                    .header("Authorization", "Bearer " + getAdminToken())
                    .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        List<UserDTO> list = new ObjectMapper().readValue(
                result.getResponse().getContentAsString(),
                new TypeReference<List<UserDTO>>(){});

        Assertions.assertNotEquals(0, list.size());
        Assertions.assertEquals(repository.count(), list.size());
    }

}