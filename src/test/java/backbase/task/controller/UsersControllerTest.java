package backbase.task.controller;

import backbase.task.dto.CreateUserDto;
import backbase.task.dto.UserDto;
import backbase.task.entity.User;
import backbase.task.service.UsersService;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UsersControllerTest {

    @Value(value = "${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @MockBean
    private UsersService usersService;

    User user1 = new User(1L, "Jan", "Nowak");
    User user2 = new User(2L, "Anna", "Nowak");
    User user3 = new User(3L, "Bruce", "Lee");
    User user4 = new User(4L, "Chuck", "Norris");

    UserDto userDTO1 = new UserDto("Jan", "Nowak");
    UserDto userDTO2 = new UserDto("Anna", "Nowak");
    UserDto userDTO3 = new UserDto("Bruce", "Lee");
    UserDto userDTO4 = new UserDto("Chuck", "Norris");

    UserDto userDTO1id = new UserDto(1L, "Jan", "Nowak");
    UserDto userDTO2id = new UserDto(2L, "Anna", "Nowak");
    UserDto userDTO3id = new UserDto(3L, "Bruce", "Lee");
    UserDto userDTO4id = new UserDto(4L, "Chuck", "Norris");

    @BeforeEach
    public void mockingUsersService() {
        when(usersService.findByLastName("Nowak")).thenReturn(List.of(user1, user2));
        when(usersService.findByLastName("Lee")).thenReturn(List.of(user3));
        when(usersService.findByLastName("Norris")).thenReturn(List.of(user4));
        when(usersService.findAll(Mockito.any(Pageable.class))).thenReturn(new PageImpl<User>(List.of(user1, user2, user3, user4)));
        when(usersService.findById(1L)).thenReturn(Optional.of(user1));
        when(usersService.findById(3L)).thenReturn(Optional.of(user3));
    }

    //test for endpoint api/users below
    @Test
    @DisplayName("\"api/users\" endpoint retrieves all users")
    public void shouldRetrieveAllUsers() {
        //when
        ResponseEntity<HelperPageResponse<UserDto>> users = testRestTemplate
                .exchange("http://localhost:" + port + "/api/users", HttpMethod.GET, null, new ParameterizedTypeReference<HelperPageResponse<UserDto>>() {});

        //then
        assertEquals(HttpStatus.OK, users.getStatusCode());
        assertEquals(List.of(userDTO1id, userDTO2id, userDTO3id, userDTO4id), users.getBody().getContent());
    }

    //2 tests for endpoint api/users/{id} below
    @Test
    @DisplayName("\"api/users/{id}\" endpoint retrieves a correct user for an existing id")
    public void shouldRetrieveUserById() {
        //when
        ResponseEntity<UserDto> user = testRestTemplate
                .exchange("http://localhost:" + port + "/api/users/1", HttpMethod.GET, null, UserDto.class);

        //then
        assertEquals(HttpStatus.OK, user.getStatusCode());
        assertEquals(userDTO1id, user.getBody());

        //when
        user = testRestTemplate.exchange(
                "http://localhost:" + port + "/api/users/3", HttpMethod.GET, null, UserDto.class);

        //then
        assertEquals(HttpStatus.OK, user.getStatusCode());
        assertEquals(userDTO3id, user.getBody());
    }

    @Test
    @DisplayName("\"api/users/{id}\" endpoint endpoint properly handles non-existing id")
    public void findByIdShouldProperlyHandleNonExistingId() {
        //when
        ResponseEntity<UserDto> users = testRestTemplate
                .exchange("http://localhost:" + port + "/api/users/9999", HttpMethod.GET, null, new ParameterizedTypeReference<UserDto>() {
                });

        //then
        assertEquals(HttpStatus.NOT_FOUND, users.getStatusCode());
    }

    //4 test for endpoint api/users-by-lastname below
    @Test
    @DisplayName("\"api/users-by-lastname\" endpoint retrieves correct users for an existing lastName")
    public void shouldRetrieveUsersByLastName() {
        //when
        ResponseEntity<List<UserDto>> users = testRestTemplate
                .exchange("http://localhost:" + port + "/api/users-by-lastname?lastName=Nowak", HttpMethod.GET, null, new ParameterizedTypeReference<List<UserDto>>() {
        });

        //then
        assertEquals(HttpStatus.OK, users.getStatusCode());
        assertEquals(List.of(userDTO1, userDTO2), users.getBody());

        //when
        users = testRestTemplate
                .exchange("http://localhost:" + port + "/api/users-by-lastname?lastName=Lee", HttpMethod.GET, null, new ParameterizedTypeReference<List<UserDto>>() {
        });

        //then
        assertEquals(HttpStatus.OK, users.getStatusCode());
        assertEquals(List.of(userDTO3), users.getBody());
    }

    @Test
    @DisplayName("\"api/users-by-lastname\" endpoint properly handles non-existing lastName")
    public void getUsersByLastNameShouldProperlyHandleNonExistingLastName() {
        //when
        ResponseEntity<List<UserDto>> users = testRestTemplate
                .exchange("http://localhost:" + port + "/api/users-by-lastname?lastName=Nonexisting", HttpMethod.GET, null, new ParameterizedTypeReference<List<UserDto>>() {
        });

        //then
        assertEquals(HttpStatus.OK, users.getStatusCode());
        assertEquals(List.of(), users.getBody());
    }

    @Test
    @DisplayName("\"api/users-by-lastname\" endpoint properly handles empty lastName")
    public void getUsersByLastNameShouldProperlyHandleEmptyLastName() {
        //when
        ResponseEntity<List<UserDto>> users = testRestTemplate
                .exchange("http://localhost:" + port + "/api/users-by-lastname?lastName=", HttpMethod.GET, null, new ParameterizedTypeReference<List<UserDto>>() {
        });

        //then
        assertEquals(HttpStatus.OK, users.getStatusCode());
        assertEquals(List.of(), users.getBody());
    }

    @Test
    @DisplayName("\"api/users-by-lastname\" endpoint properly handles not provided lastName")
    public void getUsersByLastNameShouldProperlyHandleNotProvidedLastName() {
        //when
        ResponseEntity<Object> users = testRestTemplate
                .exchange("http://localhost:" + port + "/api/users-by-lastname", HttpMethod.GET, null, Object.class);

        //then
        assertEquals(HttpStatus.BAD_REQUEST, users.getStatusCode());
    }

    //4 test for endpoint api/users-by-lastname below
    @Test
    @DisplayName("\"api/users-firstnames-by-lastname\" endpoint retrieves correct names for an existing lastName")
    public void shouldRetrieveFirstNamesByLastName() {
        //when
        ResponseEntity<List<String>> users = testRestTemplate
                .exchange("http://localhost:" + port + "/api/users-firstnames-by-lastname?lastName=Nowak", HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {
                });

        //then
        assertEquals(HttpStatus.OK, users.getStatusCode());
        assertEquals(List.of(userDTO1.getFirstName(), userDTO2.getFirstName()), users.getBody());

        //when
        users = testRestTemplate
                .exchange("http://localhost:" + port + "/api/users-firstnames-by-lastname?lastName=Lee", HttpMethod.GET, null, new ParameterizedTypeReference<List<String>>() {
                });

        //then
        assertEquals(HttpStatus.OK, users.getStatusCode());
        assertEquals(List.of(userDTO3.getFirstName()), users.getBody());
    }

    @Test
    @DisplayName("\"api/users-firstnames-by-lastname\" endpoint properly handles non-existing lastName")
    public void getFirstNamesByLastNameShouldProperlyHandleNonExistingLastName() {
        //when
        ResponseEntity<String> users = testRestTemplate
                .exchange("http://localhost:" + port + "/api/users-firstnames-by-lastname?lastName=Nonexisting", HttpMethod.GET, null, new ParameterizedTypeReference<String>() {
                });

        //then
        assertEquals(HttpStatus.OK, users.getStatusCode());
        assertEquals("[]", users.getBody());
    }

    @Test
    @DisplayName("\"api/users-firstnames-by-lastname\" endpoint properly handles empty lastName")
    public void getFirstNamesByLastNameShouldProperlyHandleEmptyLastName() {
        //when
        ResponseEntity<String> users = testRestTemplate
                .exchange("http://localhost:" + port + "/api/users-firstnames-by-lastname?lastName=", HttpMethod.GET, null, new ParameterizedTypeReference<String>() {
                });

        //then
        assertEquals(HttpStatus.OK, users.getStatusCode());
        assertEquals("[]", users.getBody());
    }

    @Test
    @DisplayName("\"api/users-firstnames-by-lastname\" endpoint properly handles not provided lastName")
    public void getFirstNamesByLastNameShouldProperlyHandleNotProvidedLastName() {
        //when
        ResponseEntity<Object> users = testRestTemplate
                .exchange("http://localhost:" + port + "/api/users-firstnames-by-lastname", HttpMethod.GET, null, Object.class);

        //then
        assertEquals(HttpStatus.BAD_REQUEST, users.getStatusCode());
    }

    //test for endpoint api/users below (POST)
    @Test
    @DisplayName("\"api/users\" endpoint creates new user (POST method)")
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

    //test for endpoint api/users below (PUT)
    @Test
    @DisplayName("\"api/users\" endpoint updates new user (PUT method)")
    public void shouldProperlyUpdateUser() {

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
