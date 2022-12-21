package backbase.task;

import backbase.task.controller.HelperPageResponse;
import backbase.task.dto.CreateUserDto;
import backbase.task.dto.PatchUserDto;
import backbase.task.dto.UserDto;
import backbase.task.entity.User;
import backbase.task.service.UsersService;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.*;
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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.jdbc.JdbcTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class E2ETest {

    @Value(value = "${local.server.port}")
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private TestRestTemplate restTemplate;
    private RestTemplate patchRestTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static boolean isInitialized = false;

    @BeforeEach
    void populateDatabase() {
        if (isInitialized) return;
        jdbcTemplate.execute("insert into users (first_name, last_name) VALUES ('Jan', 'Nowak');");
        jdbcTemplate.execute("insert into users (first_name, last_name) VALUES ('Anna', 'Nowak');");
        jdbcTemplate.execute("insert into users (first_name, last_name) VALUES ('Bruce','Lee');");
        jdbcTemplate.execute("insert into users (first_name, last_name) VALUES ('Chuck', 'Norris');");
        isInitialized = true;
    }

    UserDto userDTO1 = new UserDto("Jan", "Nowak");
    UserDto userDTO2 = new UserDto("Anna", "Nowak");
    UserDto userDTO3 = new UserDto("Bruce", "Lee");
    UserDto userDTO4 = new UserDto("Chuck", "Norris");

    UserDto userDTO1id = new UserDto(1L, "Jan", "Nowak");
    UserDto userDTO2id = new UserDto(2L, "Anna", "Nowak");
    UserDto userDTO3id = new UserDto(3L, "Bruce", "Lee");
    UserDto userDTO4id = new UserDto(4L, "Chuck", "Norris");

    //test for endpoint api/users below (GET method)
    @Test
    @Order(1)
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

    //test for endpoint api/users/{id} below (GET method)
    @Test
    @Order(2)
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

    //test for endpoint api/users-by-lastname below (GET method)
    @Test
    @Order(3)
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

    //test for endpoint api/users-firstnames-by-lastname (GET method)
    @Test
    @Order(4)
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

    //test for endpoint for POST method request to api/users below
    @Test
    @Order(5)
    @DisplayName("\"api/users\" endpoint creates new user (POST method) when request body is valid")
    public void shouldProperlyCreateUserWhenValidRequestBody() {

        //given
        User user5 = new User(5L, "Mariusz", "Pudzianowski");
//        when(usersService.save(Mockito.any(User.class))).thenReturn(user5);

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

    //test for endpoint for PUT method request to api/users/{id} below
    @Test
    @Order(6)
    @DisplayName("\"api/users/{id}\" endpoint updates user (PUT method)")
    public void shouldProperlyUpdateUserWhenExistingIdProvided() {

        //given
        User user5 = new User(5L, "Marian", "Pudzianowski");

//        when(usersService.update(Mockito.any(Long.class), Mockito.any(CreateUserDto.class))).thenReturn(Optional.of(user5));

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

    //test for endpoint for PATCH method request to api/users/{id} below
    @Test
    @Order(7)
    @DisplayName("\"api/users/{id}\" endpoint patches user when first name and last name is provided (PATCH method)")
    public void shouldProperlyPatchUserWhenFullNameProvided() {

        //given
        this.patchRestTemplate = restTemplate.getRestTemplate();
        HttpClient httpClient = HttpClientBuilder.create().build();
        this.patchRestTemplate.setRequestFactory(new HttpComponentsClientHttpRequestFactory(httpClient));

        User user5 = new User(5L, "Mariusz", "Pudzianowski");

//        when(usersService.patch(Mockito.any(Long.class), Mockito.any(PatchUserDto.class))).thenReturn(Optional.of(user5));

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

    //test for endpoint for DELETE method request to api/users/{id} below
    @Test
    @Order(8)
    @DisplayName("\"api/users\" endpoint DELETEs user valid id is provided")
    public void shouldDeleteUserWhenValidId() {

        //when
        ResponseEntity<Void> response = testRestTemplate
                .exchange("http://localhost:" + port + "/api/users/1", HttpMethod.DELETE, HttpEntity.EMPTY, Void.class);

        //then
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }
}
