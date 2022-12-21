package backbase.task.controller;

import backbase.task.dto.CreateUserDto;
import backbase.task.dto.PatchUserDto;
import backbase.task.dto.UserDto;
import backbase.task.entity.User;
import backbase.task.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UsersController {

    private final UserService userService;

    public UsersController(UserService userService) {
        this.userService = userService;
    }

    //exposing "/users" endpoint and returning list of all users (with Pageable interface - pagination and sorting)
    //including users' IDs in returned JSON - needed for updating and deleting users
    @GetMapping("/users")
    public Page<UserDto> findAll(Pageable pageable) {

        return userService
                .findAll(pageable)
                .map(user -> new UserDto(user.getId(), user.getFirstName(), user.getLastName()));
    }

    //exposing "/users/{id}" and returning a user with requested id
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> findById(@PathVariable(required = true) Long id) {
        return userService.findById(id)
                .map(user -> ResponseEntity.ok(new UserDto(user.getId(), user.getFirstName(), user.getLastName())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    //mapping for 1st required endpoint (GET list of users with given last name)
    //with duplicate name-surname pairs (as they represent different users - based on IDs)
    @GetMapping("/users-by-lastname")
    public ResponseEntity<List<UserDto>> getUsersByLastName(@RequestParam(required = true) String lastName) {

        if (lastName.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        final List<UserDto> users = userService.findByLastName(lastName)
                .stream()
                .map(user -> new UserDto(user.getFirstName(), user.getLastName()))
                //          .distinct()
                .collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }

    //mapping for 2nd required endpoint (GET list of names only for the given last name) - with distinct names for a given surname
    @GetMapping("/users-firstnames-by-lastname")
    public ResponseEntity<List<String>> getFirstNamesByLastName(@RequestParam(required = true) String lastName) {

        if (lastName.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        final List<String> users = userService.findByLastName(lastName)
                .stream()
                .map(User::getFirstName)
                .distinct()
                .collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }

    // mapping for 3rd required endpoint (POST - adding a new user) - allowing duplicate records in DB (with different IDs)
    @PostMapping("/users")
    public UserDto addUser(@RequestBody @Valid CreateUserDto newUser) {

        final User user = userService.save(new User(newUser.getFirstName(), newUser.getLastName()));

        return new UserDto(user.getId(), user.getFirstName(), user.getLastName());
    }

    // mapping for PUT /users/{id} - update existing user (requires providing all the user's fields)
    @PutMapping("/users/{id}")
    public ResponseEntity<UserDto> updateUser(@RequestBody @Valid CreateUserDto updatedUser, @PathVariable Long id) {

        return userService.update(id, updatedUser)
                .map(user -> ResponseEntity.ok(new UserDto(user.getId(), user.getFirstName(), user.getLastName())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // mapping for PATCH /users/{id} - update existing user (doesn't require providing all the user's fields)
    @PatchMapping("/users/{id}")
    public ResponseEntity<UserDto> patchUser(@RequestBody @Valid PatchUserDto updatedUser, @PathVariable Long id) {

        return userService.patch(id, updatedUser)
                .map(user -> ResponseEntity.ok(new UserDto(user.getId(), user.getFirstName(), user.getLastName())))
                .orElseGet(() -> ResponseEntity.notFound().build());

    }

    // mapping for DELETE /users/{id}
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {

        final Optional<User> userToDelete = userService.findById(id);

        if (userToDelete.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        userService.deleteById(id);

        return ResponseEntity.ok().build();
    }
}
