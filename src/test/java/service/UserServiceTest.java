package service;

import backbase.task.db.UsersRepository;
import backbase.task.dto.PatchUserDto;
import backbase.task.entity.User;
import backbase.task.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockBean
    private UsersRepository usersRepository;

    User user1 = new User(1L, "Jan", "Nowak");
    User user2 = new User(2L, "Anna", "Nowak");
    User user3 = new User(3L, "Bruce", "Lee");
    User user4 = new User(4L, "Chuck", "Norris");

    @BeforeEach
    public void mockingUsersRepository() {
        when(usersRepository.findByLastNameIgnoreCase(user1.getLastName())).thenReturn(List.of(user1, user2));
        when(usersRepository.findByLastNameIgnoreCase(user2.getLastName())).thenReturn(List.of(user1, user2));
        when(usersRepository.findByLastNameIgnoreCase(user3.getLastName())).thenReturn(List.of(user3));
        when(usersRepository.findByLastNameIgnoreCase(user4.getLastName())).thenReturn(List.of(user4));

        when(usersRepository.findAll(Mockito.any(Pageable.class))).thenReturn(new PageImpl<User>(List.of(user1, user2, user3, user4)));

        when(usersRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
    }

    @AfterEach
    void cleanDb() {
        jdbcTemplate.execute("delete from users;");
    }

    @Test
    @DisplayName("Method findAll works properly")
    public void shouldFindAll() {
        Page<User> retrievedPage = userService.findAll(PageRequest.of(0, 20, Sort.unsorted()));
        List<User> users = retrievedPage.get().collect(Collectors.toList());

        assertEquals(4, users.size());
    }

    @Test
    @DisplayName("Method findByLastName works properly")
    public void shouldFindByLastName() {
        List<User> foundUsers = userService.findByLastName("Nowak");
        assertEquals(List.of(user1, user2), foundUsers);
    }

    @Test
    @DisplayName("Method save works properly")
    public void shouldSave() {
        User user5 = new User(5L, "Arnold", "Schwarzeneger");

        userService.save(user5);
        verify(usersRepository, times(1)).save(user5);
    }

    @Test
    @DisplayName("Method findById works properly")
    public void shouldFindById() {
        Optional<User> foundUser = userService.findById(1L);
        assertEquals(user1, foundUser.get());
    }

    @Test
    @DisplayName("Method patch works properly")
    public void shouldPatch() {
        PatchUserDto patchUserDto = new PatchUserDto();
        patchUserDto.setFirstName("Fred");

        when(usersRepository.save(Mockito.any(User.class))).thenReturn(user1);

        Optional<User> updatedUser = userService.patch(1L,patchUserDto);
        assertTrue(updatedUser.isPresent());
        assertEquals("Fred", updatedUser.get().getFirstName());
        assertEquals(user1.getLastName(), updatedUser.get().getLastName());
    }
}
