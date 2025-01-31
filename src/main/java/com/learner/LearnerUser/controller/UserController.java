package com.learner.LearnerUser.controller;

import java.util.List;

import com.learner.LearnerUser.entity.User;
import com.learner.LearnerUser.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // get all users
    @GetMapping
    public List<User> getAllUsers() {
        return this.userRepository.findAll();
    }

    @GetMapping("/getUserById")
    public User getUserByUserId(@RequestParam long userId, HttpServletResponse response) {
        User user = this.userRepository.findById(userId);
        if (null == user) {
            response.setStatus(HttpStatus.NO_CONTENT.value());
        }
        return user;
    }

    // get user by email and password
    @CrossOrigin(origins = "*")
    @GetMapping("/getDetails")
    public UserByEmail getUserById(@RequestParam String email, String password) {
        User existingUser = userRepository.findByEmailAndPassword(email, password);
        if (existingUser == null) {
            throw new RuntimeException("Email or password is incorrect");
        }
        UserByEmail userByEmail = new UserByEmail();
        userByEmail.setEmail(existingUser.getEmail());
        userByEmail.setFirstName(existingUser.getFirstName());
        userByEmail.setLastName(existingUser.getLastName());
        userByEmail.setId(existingUser.getUserId());
        return userByEmail;
    }


    // create user
    @CrossOrigin(origins = "*")
    @PostMapping
    public User createUser(@RequestBody User user) {
        User existingUser = userRepository.findByEmail(user.getEmail());
        if (existingUser != null) {
            // record with this email already exists, throw an exception or return an error
            throw new RuntimeException("User with email " + user.getEmail() + " already exists");
        }
        return this.userRepository.save(user);
    }
}
