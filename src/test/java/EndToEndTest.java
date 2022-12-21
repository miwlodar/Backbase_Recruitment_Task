import backbase.task.dto.UserDto;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EndToEndTest {

    @Value(value = "${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

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

    UserDto userDTO1 = new UserDto("Jan", "Nowak");
    UserDto userDTO2 = new UserDto("Anna", "Nowak");
    UserDto userDTO3 = new UserDto("Bruce", "Lee");
    UserDto userDTO4 = new UserDto("Chuck", "Norris");

    @Test
    @DisplayName("\"api/users-by-lastname\" endpoint retrieves correct users for an existing lastName")
    public void shouldRetrieveUsersByLastName() {
        //when
        ResponseEntity<List<UserDto>> users = testRestTemplate.exchange("http://localhost:" + port + "/api/users-by-lastname?lastName=Nowak", HttpMethod.GET, null, new ParameterizedTypeReference<List<UserDto>>() {
        });

        //then
        assertEquals(HttpStatus.OK, users.getStatusCode());
        assertEquals(List.of(userDTO1, userDTO2), users.getBody());

        //when
        users = testRestTemplate.exchange("http://localhost:" + port + "/api/users-by-lastname?lastName=Lee", HttpMethod.GET, null, new ParameterizedTypeReference<List<UserDto>>() {
        });

        //then
        assertEquals(HttpStatus.OK, users.getStatusCode());
        assertEquals(List.of(userDTO3), users.getBody());
    }

    @Test
    @DisplayName("\"api/users-by-lastname\" endpoint retrieves an empty list for a non-existing lastName")
    public void shouldProperlyHandleNonExistingLastName() {
        //when
        ResponseEntity<List<UserDto>> users = testRestTemplate.exchange("http://localhost:" + port + "/api/users-by-lastname?lastName=Nonexisting", HttpMethod.GET, null, new ParameterizedTypeReference<List<UserDto>>() {
        });

        //then
        assertEquals(HttpStatus.OK, users.getStatusCode());
        assertEquals(List.of(), users.getBody());
    }

    @Test
    @DisplayName("\"api/users-by-lastname\" endpoint retrieves an empty list for empty lastName")
    public void shouldProperlyHandleEmptyLastName() {
        //when
        ResponseEntity<List<UserDto>> users = testRestTemplate.exchange("http://localhost:" + port + "/api/users-by-lastname?lastName=", HttpMethod.GET, null, new ParameterizedTypeReference<List<UserDto>>() {
        });

        //then
        assertEquals(HttpStatus.OK, users.getStatusCode());
        assertEquals(List.of(), users.getBody());
    }

    @Test
    @DisplayName("\"api/users-by-lastname\" endpoint returns BAD REQUEST lastName is not provided")
    public void shouldProperlyHandleNotProvidedLastName() {
        //when
        ResponseEntity<Object> users = testRestTemplate.exchange("http://localhost:" + port + "/api/users-by-lastname", HttpMethod.GET, null, Object.class);

        //then
        assertEquals(HttpStatus.BAD_REQUEST, users.getStatusCode());
    }
}
