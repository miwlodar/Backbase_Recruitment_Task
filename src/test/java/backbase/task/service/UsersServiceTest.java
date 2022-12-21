package backbase.task.service;

import backbase.task.db.UsersRepository;
import backbase.task.dto.CreateUserDto;
import backbase.task.dto.PatchUserDto;
import backbase.task.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SpringBootTest
class UsersServiceTest {

    @Autowired
    private UsersService usersService;

    @MockBean
    private UsersRepository usersRepository;

    User user1 = new User(1L, "Jan", "Nowak");
    User user2 = new User(2L, "Anna", "Nowak");
    User user3 = new User(3L, "Bruce", "Lee");
    User user4 = new User(4L, "Chuck", "Norris");

    @BeforeEach
    public void mockingUsersRepository() {
        when(usersRepository.findAll(Mockito.any(Pageable.class))).thenReturn(new PageImpl<User>(List.of(user1, user2, user3, user4)));
        when(usersRepository.findAll()).thenReturn(List.of(user1, user2, user3, user4));
        when(usersRepository.findById(user1.getId())).thenReturn(Optional.of(user1));
        when(usersRepository.findByLastNameIgnoreCase(user1.getLastName())).thenReturn(List.of(user1, user2));
        when(usersRepository.findByLastNameIgnoreCase(user2.getLastName())).thenReturn(List.of(user1, user2));
        when(usersRepository.findByLastNameIgnoreCase(user3.getLastName())).thenReturn(List.of(user3));
        when(usersRepository.findByLastNameIgnoreCase(user4.getLastName())).thenReturn(List.of(user4));
    }

    @Test
    @DisplayName("Method findAll works properly")
    public void shouldFindAllUsers() {
        Page<User> retrievedPage = usersService.findAll(PageRequest.of(0, 20, Sort.unsorted()));
        List<User> users = retrievedPage.get().collect(Collectors.toList());

        assertEquals(4, users.size());
        assertEquals(List.of(user1, user2, user3, user4), users);
    }

    @Test
    @DisplayName("Method findById works properly")
    public void shouldFindUserById() {
        Optional<User> foundUser = usersService.findById(1L);
        assertEquals(user1, foundUser.get());
    }

    @Test
    @DisplayName("Method findByLastName works properly")
    public void shouldFindUsersByLastName() {
        List<User> foundUsers = usersService.findByLastName("Nowak");
        assertEquals(List.of(user1, user2), foundUsers);
    }

    @Test
    @DisplayName("Method save works properly")
    public void shouldSaveUser() {
        User user5 = new User(5L, "Arnold", "Schwarzeneger");

        usersService.save(user5);
        verify(usersRepository, times(1)).save(user5);
    }

    @Test
    @DisplayName("Method update works properly")
    public void shouldUpdateUser() {
        CreateUserDto createUserDto = new CreateUserDto();
        createUserDto.setFirstName("Fred");
        createUserDto.setLastName("Flintstone");

        when(usersRepository.save(Mockito.any(User.class))).thenReturn(user1);

        Optional<User> updatedUser = usersService.update(1L, createUserDto);
        assertTrue(updatedUser.isPresent());
        assertEquals("Fred", updatedUser.get().getFirstName());
        assertEquals("Flintstone", updatedUser.get().getLastName());
    }

    @Test
    @DisplayName("Method patch works properly")
    public void shouldPatchUser() {
        PatchUserDto patchUserDto = new PatchUserDto();
        patchUserDto.setFirstName("Fred");

        when(usersRepository.save(Mockito.any(User.class))).thenReturn(user1);

        Optional<User> patchedUser = usersService.patch(1L, patchUserDto);
        assertTrue(patchedUser.isPresent());
        assertEquals("Fred", patchedUser.get().getFirstName());
        assertEquals(user1.getLastName(), patchedUser.get().getLastName());
    }

    @Test
    @DisplayName("Method delete works properly")
    public void shouldDeleteUser() {

        usersService.deleteById(1L);

        verify(usersRepository, times(1)).deleteById(1L);
    }
}
