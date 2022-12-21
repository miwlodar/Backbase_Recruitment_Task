package db;

import backbase.task.entity.User;
import backbase.task.db.UsersRepository;
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

@DataJpaTest
class UsersRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UsersRepository usersRepository;

    @BeforeEach
    void addUser() {
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
    void shouldFindAll() throws Exception {
        Page<User> retrievedPage = usersRepository.findAll(PageRequest.of(0, 20, Sort.unsorted()));
        List<User> users = retrievedPage.get().collect(Collectors.toList());

        assertEquals(4, users.size());
    }

    @Test
    @DisplayName("Method findByLastNameIgnoreCase works properly")
    void shouldFindByLastName() throws Exception {
        User retrievedUser = usersRepository.findByLastNameIgnoreCase("Nowak").get(0);
        assertEquals(1, retrievedUser.getId());
        assertEquals("Jan", retrievedUser.getFirstName());
        assertEquals("Nowak", retrievedUser.getLastName());
    }
}
