package com.learner.LearnerUser;

import com.learner.LearnerUser.controller.UserByEmail;
import com.learner.LearnerUser.controller.UserController;
import com.learner.LearnerUser.entity.User;
import com.learner.LearnerUser.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserController userController;

    private User user1;
    private User user2;

    @Before
    public void setUp() {
        user1 = new User();
        user1.setUserId(1L);
        user1.setEmail("test1@test.com");
        user1.setFirstName("Test1");
        user1.setLastName("User1");
        user1.setPassword("password1");

        user2 = new User();
        user2.setUserId(2L);
        user2.setEmail("test2@test.com");
        user2.setFirstName("Test2");
        user2.setLastName("User2");
        user2.setPassword("password2");
    }

    @Test
    public void testGetAllUsers() {
        // given
        List<User> userList = new ArrayList<>();
        userList.add(user1);
        userList.add(user2);
        when(userRepository.findAll()).thenReturn(userList);

        // when
        List<User> result = userController.getAllUsers();

        // then
        assertEquals(2, result.size());
        assertTrue(result.contains(user1));
        assertTrue(result.contains(user2));
    }

    @Test
    public void testGetUserByUserId() {
        // given
        when(userRepository.findById(1L)).thenReturn(user1);

        // when
        User result = userController.getUserByUserId(1L, null);

        // then
        assertEquals(user1, result);
    }

    @Test
    public void testGetUserByUserIdNotFound() {
        // given
        when(userRepository.findById(3L)).thenReturn(null);

        // when
        HttpServletResponse response = mock(HttpServletResponse.class);
        User result = userController.getUserByUserId(3L, response);

        // then
        assertNull(result);
        verify(response, times(1)).setStatus(HttpStatus.NO_CONTENT.value());


    }

    @Test
    public void testGetUserById() {
        // given
        when(userRepository.findByEmailAndPassword("test1@test.com", "password1")).thenReturn(user1);

        // when
        UserByEmail result = userController.getUserById("test1@test.com", "password1");

        // then
        assertEquals(user1.getUserId(), result.getId());
        assertEquals(user1.getFirstName(), result.getFirstName());
        assertEquals(user1.getLastName(), result.getLastName());
        assertEquals(user1.getEmail(), result.getEmail());
    }

    @Test(expected = RuntimeException.class)
    public void testGetUserByIdIncorrectCredentials() {
        // given
        when(userRepository.findByEmailAndPassword("test1@test.com", "password1")).thenReturn(null);

        // when
        userController.getUserById("test1@test.com", "password1");

        // then
        // expect RuntimeException to be thrown
    }

    @Test
    public void testCreateUser() {
        // given
        when(userRepository.save(user1)).thenReturn(user1);

        // when
        User result = userController.createUser(user1);

        // then
        assertEquals(user1, result);
        verify(userRepository, times(1)).save(user1);
    }

    @Test(expected = RuntimeException.class)
    public void testCreateUserAlreadyExists() {
        // given
        when(userRepository.findByEmail("test1@test.com")).thenReturn(user1);

        // when
        userController.createUser(user1);

        // then
        // expect RuntimeException to be thrown
    }

    @Test
    public void testConstructor() {
        String firstName = "John";
        String lastName = "Doe";
        String email = "john.doe@example.com";
        String password = "password";

        User user = new User(firstName, lastName, email, password);

        assertEquals(firstName, user.getFirstName());
        assertEquals(lastName, user.getLastName());
        assertEquals(email, user.getEmail());
        assertEquals(password, user.getPassword());
    }
}
