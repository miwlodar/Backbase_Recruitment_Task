package backbase.task.controller;

import backbase.task.dto.CreateUserDto;
import backbase.task.dto.PatchUserDto;
import backbase.task.dto.UserDto;
import backbase.task.entity.User;
import backbase.task.service.UsersService;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
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
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

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

    @Autowired
    private TestRestTemplate restTemplate;
    private RestTemplate patchRestTemplate;

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

    //test for endpoint api/users below (GET method)
    @Test
    @DisplayName("\"api/users\" endpoint retrieves all users")
    public void shouldRetrieveAllUsers() {
        //when
        ResponseEntity<HelperPageResponse<UserDto>> users = testRestTemplate
                .exchange("http://localhost:" + port + "/api/users", HttpMethod.GET, null, new ParameterizedTypeReference<HelperPageResponse<UserDto>>() {
                });

        //then
        assertEquals(HttpStatus.OK, users.getStatusCode());
        assertEquals(List.of(userDTO1id, userDTO2id, userDTO3id, userDTO4id), users.getBody().getContent());
    }

    //2 tests for endpoint api/users/{id} below (GET method)
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

    //4 test for endpoint api/users-by-lastname below (GET method)
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

    //4 test for endpoint api/users-firstnames-by-lastname below (GET method)
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

    //4 tests for endpoint for POST method request to api/users below
    @Test
    @DisplayName("\"api/users\" endpoint creates new user (POST method) when request body is valid")
    public void shouldProperlyCreateUserWhenValidRequestBody() {

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

    @Test
    @DisplayName("\"api/users\" endpoint properly handles missing last name in request body (POST method)")
    public void shouldProperlyCreateUserWhenMissingLastName() {

        //given
        User user5 = new User(5L, "Mariusz", null);
        when(usersService.save(Mockito.any(User.class))).thenReturn(user5);

        //when
        HttpEntity<CreateUserDto> request = new HttpEntity<>(new CreateUserDto("Mariusz", null));
        ResponseEntity<UserDto> response = testRestTemplate
                .exchange("http://localhost:" + port + "/api/users", HttpMethod.POST, request, UserDto.class);

        //then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("\"api/users\" endpoint properly handles too short last name in request body (POST method)")
    public void shouldProperlyCreateUserWhenLastNameTooShort() {

        //given
        User user5 = new User(5L, "Mariusz", "P");
        when(usersService.save(Mockito.any(User.class))).thenReturn(user5);

        //when
        HttpEntity<CreateUserDto> request = new HttpEntity<>(new CreateUserDto("Mariusz", "P"));
        ResponseEntity<UserDto> response = testRestTemplate
                .exchange("http://localhost:" + port + "/api/users", HttpMethod.POST, request, UserDto.class);

        //then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("\"api/users\" endpoint properly handles too long last name in request body (POST method)")
    public void shouldProperlyCreateUserWhenLastNameTooLong() {

        //given
        User user5 = new User(5L, "Mariusz", "Pudzianowskiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii");
        when(usersService.save(Mockito.any(User.class))).thenReturn(user5);

        //when
        HttpEntity<CreateUserDto> request = new HttpEntity<>(new CreateUserDto("Mariusz", "Pudzianowskiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii"));
        ResponseEntity<UserDto> response = testRestTemplate
                .exchange("http://localhost:" + port + "/api/users", HttpMethod.POST, request, UserDto.class);

        //then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    //5 tests for endpoint for PUT method request to api/users/{id} below
    @Test
    @DisplayName("\"api/users/{id}\" endpoint updates user (PUT method)")
    public void shouldProperlyUpdateUserWhenExistingIdProvided() {

        //given
        User user5 = new User(5L, "Marian", "Pudzianowski");

        when(usersService.update(Mockito.any(Long.class), Mockito.any(CreateUserDto.class))).thenReturn(Optional.of(user5));

        //when
        HttpEntity<CreateUserDto> request = new HttpEntity<>(new CreateUserDto("Marian", "Pudzianowski"));
        ResponseEntity<UserDto> response = testRestTemplate
                .exchange("http://localhost:" + port + "/api/users/5", HttpMethod.PUT, request, UserDto.class);

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Marian", response.getBody().getFirstName());
        assertEquals("Pudzianowski", response.getBody().getLastName());
        assertNotNull(response.getBody().getId());
    }

    @Test
    @DisplayName("\"api/users/{id}\" endpoint properly handles invalid ID (PUT method)")
    public void shouldProperlyHandleNonExistingIdProvided() {

        //when
        HttpEntity<CreateUserDto> request = new HttpEntity<>(new CreateUserDto("Marian", "Pudzianowski"));
        ResponseEntity<UserDto> response = testRestTemplate
                .exchange("http://localhost:" + port + "/api/users/99", HttpMethod.PUT, request, UserDto.class);

        //then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    @DisplayName("\"api/users/{id}\" endpoint properly handles missing last name (PUT method)")
    public void shouldProperlyHandleMissingLastName() {

        //given
        User user5 = new User(5L, "Marian", null);

        when(usersService.update(Mockito.any(Long.class), Mockito.any(CreateUserDto.class))).thenReturn(Optional.of(user5));

        //when
        HttpEntity<CreateUserDto> request = new HttpEntity<>(new CreateUserDto("Marian", null));
        ResponseEntity<UserDto> response = testRestTemplate
                .exchange("http://localhost:" + port + "/api/users/5", HttpMethod.PUT, request, UserDto.class);

        //then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("\"api/users/{id}\" endpoint properly handles too short last name (PUT method)")
    public void shouldProperlyHandleTooShortLastName() {

        //given
        User user5 = new User(5L, "Marian", "P");

        when(usersService.update(Mockito.any(Long.class), Mockito.any(CreateUserDto.class))).thenReturn(Optional.of(user5));

        //when
        HttpEntity<CreateUserDto> request = new HttpEntity<>(new CreateUserDto("Marian", "P"));
        ResponseEntity<UserDto> response = testRestTemplate
                .exchange("http://localhost:" + port + "/api/users/5", HttpMethod.PUT, request, UserDto.class);

        //then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("\"api/users/{id}\" endpoint properly handles too long last name (PUT method)")
    public void shouldProperlyHandleTooLongLastName() {

        //given
        User user5 = new User(5L, "Marian", "Pudzianowskiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii");

        when(usersService.update(Mockito.any(Long.class), Mockito.any(CreateUserDto.class))).thenReturn(Optional.of(user5));

        //when
        HttpEntity<CreateUserDto> request = new HttpEntity<>(new CreateUserDto("Marian", "Pudzianowskiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii"));
        ResponseEntity<UserDto> response = testRestTemplate
                .exchange("http://localhost:" + port + "/api/users/5", HttpMethod.PUT, request, UserDto.class);

        //then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    //4 tests for endpoint for PATCH method request to api/users/{id} below
    @Test
    @DisplayName("\"api/users/{id}\" endpoint patches user when first name and last name is provided (PATCH method)")
    public void shouldProperlyPatchUserWhenFullNameProvided() {

        //given
        this.patchRestTemplate = restTemplate.getRestTemplate();
        HttpClient httpClient = HttpClientBuilder.create().build();
        this.patchRestTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));

        User user5 = new User(5L, "Mariusz", "Pudzianowski");

        when(usersService.patch(Mockito.any(Long.class), Mockito.any(PatchUserDto.class))).thenReturn(Optional.of(user5));

        //when
        HttpEntity<PatchUserDto> request = new HttpEntity<>(new PatchUserDto("Mariusz", "Pudzianowski"));
        ResponseEntity<UserDto> response = patchRestTemplate
                .exchange("http://localhost:" + port + "/api/users/5", HttpMethod.PATCH, request, UserDto.class);

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Mariusz", response.getBody().getFirstName());
        assertEquals("Pudzianowski", response.getBody().getLastName());
        assertNotNull(response.getBody().getId());
    }

    @Test
    @DisplayName("\"api/users/{id}\" endpoint patches user when only first name is provided (PATCH method)")
    public void shouldProperlyPatchUserWhenOnlyFirstNameProvided() {

        //given
        this.patchRestTemplate = restTemplate.getRestTemplate();
        HttpClient httpClient = HttpClientBuilder.create().build();
        this.patchRestTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));

        User user5 = new User(1L, "Mariusz", null);

        when(usersService.patch(Mockito.any(Long.class), Mockito.any(PatchUserDto.class))).thenReturn(Optional.of(new User(1L, user5.getFirstName(), user1.getLastName())));

        //when
        HttpEntity<PatchUserDto> request = new HttpEntity<>(new PatchUserDto("Mariusz", null));
        ResponseEntity<UserDto> response = patchRestTemplate
                .exchange("http://localhost:" + port + "/api/users/1", HttpMethod.PATCH, request, UserDto.class);

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Mariusz", response.getBody().getFirstName());
        assertEquals("Nowak", response.getBody().getLastName());
        assertEquals(1, response.getBody().getId());
    }

    @Test
    @DisplayName("\"api/users/{id}\" endpoint properly handles too short first name (PATCH method)")
    public void shouldProperlyHandleWhenFirstNameTooShort() {

        //given
        this.patchRestTemplate = restTemplate.getRestTemplate();
        HttpClient httpClient = HttpClientBuilder.create().build();
        this.patchRestTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));

        User user5 = new User(1L, "M", null);

        when(usersService.patch(Mockito.any(Long.class), Mockito.any(PatchUserDto.class))).thenReturn(Optional.of(new User(1L, user5.getFirstName(), user1.getLastName())));

        //when
        HttpEntity<PatchUserDto> request = new HttpEntity<>(new PatchUserDto("M", null));
        ResponseEntity<UserDto> response = patchRestTemplate
                .exchange("http://localhost:" + port + "/api/users/1", HttpMethod.PATCH, request, UserDto.class);

        //then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    @DisplayName("\"api/users/{id}\" endpoint properly handles too long first name (PATCH method)")
    public void shouldProperlyHandleWhenFirstNameTooLong() {

        //given
        this.patchRestTemplate = restTemplate.getRestTemplate();
        HttpClient httpClient = HttpClientBuilder.create().build();
        this.patchRestTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));

        User user5 = new User(1L, "Mariiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiusz", null);

        when(usersService.patch(Mockito.any(Long.class), Mockito.any(PatchUserDto.class))).thenReturn(Optional.of(new User(1L, user5.getFirstName(), user1.getLastName())));

        //when
        HttpEntity<PatchUserDto> request = new HttpEntity<>(new PatchUserDto("Mariiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiusz", null));
        ResponseEntity<UserDto> response = patchRestTemplate
                .exchange("http://localhost:" + port + "/api/users/1", HttpMethod.PATCH, request, UserDto.class);

        //then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    //2 tests for endpoint for DELETE method request to api/users/{id} below
    @Test
    @DisplayName("\"api/users\" endpoint DELETEs user valid id is provided")
    public void shouldDeleteUserWhenValidId() {

        //when
        ResponseEntity<Void> response = testRestTemplate
                .exchange("http://localhost:" + port + "/api/users/1", HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("\"api/users\" endpoint properly handles invalid id (DELETE method)")
    public void shouldProperlyHandleDeleteWithInvalidId() {

        //when
        ResponseEntity<Void> response = testRestTemplate
                .exchange("http://localhost:" + port + "/api/users/99", HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);

        //then
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
