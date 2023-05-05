package dv.kinash.hw15.service;

import dv.kinash.hw15.dto.UserDTO;
import dv.kinash.hw15.dto.UserNewDTO;
import dv.kinash.hw15.repository.entity.User;

import java.util.List;

public interface UserService {
    public void addUser(UserNewDTO newUser, Boolean isAdmin);
    public List<UserDTO> getUsers();
    public User getUserByEmail(String email);
    public void checkAdmin();
}
