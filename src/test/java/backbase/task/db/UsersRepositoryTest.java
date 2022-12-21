package backbase.task.db;

import backbase.task.entity.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class UsersRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UsersRepository usersRepository;

    @BeforeEach
    void addUsers() {
        jdbcTemplate.execute("insert into users (first_name, last_name) VALUES ('Jan', 'Nowak');");
        jdbcTemplate.execute("insert into users (first_name, last_name) VALUES ('Anna', 'Nowak');");
        jdbcTemplate.execute("insert into users (first_name, last_name) VALUES ('Bruce','Lee');");
        jdbcTemplate.execute("insert into users (first_name, last_name) VALUES ('Chuck', 'Norris');");
    }

    @AfterEach
    void cleanDb() {
        jdbcTemplate.execute("delete from users;");
    }

    @Test
    @DisplayName("Method findAll works properly")
    void shouldFindAllUsers() throws Exception {
        Page<User> retrievedPage = usersRepository.findAll(PageRequest.of(0, 20, Sort.unsorted()));
        List<User> users = retrievedPage.get().collect(Collectors.toList());
        List<String> usersFirstNames = users.stream().map(User::getFirstName).collect(Collectors.toList());
        List<String> usersLastNames = users.stream().map(User::getLastName).collect(Collectors.toList());

        assertEquals(4, users.size());
        assertTrue(usersFirstNames.contains("Jan"));
        assertTrue(usersLastNames.contains("Nowak"));
        assertTrue(usersFirstNames.contains("Anna"));
        assertTrue(usersLastNames.contains("Nowak"));
        assertTrue(usersFirstNames.contains("Bruce"));
        assertTrue(usersLastNames.contains("Lee"));
        assertTrue(usersFirstNames.contains("Chuck"));
        assertTrue(usersLastNames.contains("Norris"));
    }

    @Test
    @DisplayName("Method findByLastNameIgnoreCase works properly")
    void shouldFindUsersByLastName() throws Exception {
        User retrievedUser = usersRepository.findByLastNameIgnoreCase("Nowak").get(0);
        assertEquals(5, retrievedUser.getId());
        assertEquals("Jan", retrievedUser.getFirstName());
        assertEquals("Nowak", retrievedUser.getLastName());

        User retrievedUser2 = usersRepository.findByLastNameIgnoreCase("Lee").get(0);
        assertEquals(7, retrievedUser2.getId());
        assertEquals("Bruce", retrievedUser2.getFirstName());
        assertEquals("Lee", retrievedUser2.getLastName());
    }
}
