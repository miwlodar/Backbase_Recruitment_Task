package controller;

import backbase.task.dto.CreateUserDto;
import backbase.task.dto.UserDto;
import backbase.task.entity.User;
import backbase.task.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ControllerTest {

    @Value(value = "${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @MockBean
    private UserService usersService;

    User user1 = new User(1L, "Jan", "Nowak");
    User user2 = new User(2L, "Anna", "Nowak");
    User user3 = new User(3L, "Bruce", "Lee");
    User user4 = new User(4L, "Chuck", "Norris");

    UserDto userDTO1 = new UserDto("Jan", "Nowak");
    UserDto userDTO2 = new UserDto("Anna", "Nowak");
    UserDto userDTO3 = new UserDto("Bruce", "Lee");
    UserDto userDTO4 = new UserDto("Chuck", "Norris");

    @BeforeEach
    public void mockingUsersService() {
        when(usersService.findByLastName("Nowak")).thenReturn(List.of(user1, user2));
        when(usersService.findByLastName("Lee")).thenReturn(List.of(user3));
        when(usersService.findByLastName("Norris")).thenReturn(List.of(user4));
    }

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
    @DisplayName("\"api/users-by-lastname\" endpoint retrieves empty list for a non-existing lastName")
    public void shouldProperlyHandleNonExistingLastName() {
        //when
        ResponseEntity<List<UserDto>> users = testRestTemplate.exchange("http://localhost:" + port + "/api/users-by-lastname?lastName=Nonexisting", HttpMethod.GET, null, new ParameterizedTypeReference<List<UserDto>>() {
        });

        //then
        assertEquals(HttpStatus.OK, users.getStatusCode());
        assertEquals(List.of(), users.getBody());
    }

    @Test
    @DisplayName("\"api/users-by-lastname\" endpoint retrieves list for an empty lastName")
    public void shouldProperlyHandleEmptyLastName() {
        //when
        ResponseEntity<List<UserDto>> users = testRestTemplate.exchange("http://localhost:" + port + "/api/users-by-lastname?lastName=", HttpMethod.GET, null, new ParameterizedTypeReference<List<UserDto>>() {
        });

        //then
        assertEquals(HttpStatus.OK, users.getStatusCode());
        assertEquals(List.of(), users.getBody());
    }

    @Test
    @DisplayName("\"api/users-by-lastname\" endpoint retrieves correct users when last-name is not provided")
    public void shouldProperlyHandleNotProvidedLastName() {
        //when
        ResponseEntity<Object> users = testRestTemplate.exchange("http://localhost:" + port + "/api/users-by-lastname", HttpMethod.GET, null, Object.class);

        //then
        assertEquals(HttpStatus.BAD_REQUEST, users.getStatusCode());
    }

    @Test
    @DisplayName("\"api/users\" endpoint creates user properly")
    public void shouldProperlyCreateUser() {

        //given
        User user5 = new User(5L, "Mariusz", "Pudzianowski");
        when(usersService.save(Mockito.any(User.class))).thenReturn(user5);

        //when
        HttpEntity<CreateUserDto> request = new HttpEntity<>(new CreateUserDto("Mariusz", "Pudzianowski"));
        ResponseEntity<UserDto> response = testRestTemplate
                .exchange("http://localhost:" + port + "/api/users", HttpMethod.POST, request, UserDto.class);

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Mariusz", response.getBody().getFirstName());
        assertEquals("Pudzianowski", response.getBody().getLastName());
        assertNotNull(response.getBody().getId());
    }
}
