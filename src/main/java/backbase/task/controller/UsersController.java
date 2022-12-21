package backbase.task.controller;

import backbase.task.dto.CreateUserDto;
import backbase.task.dto.PatchUserDto;
import backbase.task.dto.UserDto;
import backbase.task.entity.User;
import backbase.task.service.UsersService;
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

    private final UsersService usersService;

    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    /**
     * @return list of all users (with Pageable interface - enabling pagination and sorting)
     * including users' IDs in returned JSON - needed for updating/patching and deleting users
     */
    @GetMapping("/users")
    public ResponseEntity<Page<UserDto>> findAll(Pageable pageable) {

        return ResponseEntity.ok(usersService
                .findAll(pageable)
                .map(user -> new UserDto(user.getId(), user.getFirstName(), user.getLastName())));
    }

    /**
     * @param id id of the requested user
     * @return requested user
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> findById(@PathVariable(required = true) Long id) {
        return usersService.findById(id)
                .map(user -> ResponseEntity.ok(new UserDto(user.getId(), user.getFirstName(), user.getLastName())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * mapping for 1st endpoint from the requirements
     * @param lastName user's last name (by adding ?lastName={lastName} to the URL)
     * @return list of users with requested last name
     * with duplicate first and last name pairs (as they represent different users, based on IDs) - .distinct() may be uncommented to modify it
     */
    @GetMapping("/users-by-lastname")
    public ResponseEntity<List<UserDto>> getUsersByLastName(@RequestParam(required = true) String lastName) {

        if (lastName.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        final List<UserDto> users = usersService.findByLastName(lastName)
                .stream()
                .map(user -> new UserDto(user.getFirstName(), user.getLastName()))
                //          .distinct()
                .collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }

    /**
     * mapping for 2nd endpoint from the requirements
     * @param lastName user's last name (by adding ?lastName={lastName} to the URL)
     * @return list of users' first names only for the given last name
     * with distinct first names for a given last name - .distinct() may be commented out to modify it
     */
    @GetMapping("/users-firstnames-by-lastname")
    public ResponseEntity<List<String>> getFirstNamesByLastName(@RequestParam(required = true) String lastName) {

        if (lastName.isEmpty()) {
            return ResponseEntity.ok(Collections.emptyList());
        }

        final List<String> users = usersService.findByLastName(lastName)
                .stream()
                .map(User::getFirstName)
                .distinct()
                .collect(Collectors.toList());

        return ResponseEntity.ok(users);
    }

    /**
     * mapping for 3rd endpoint from the requirements (with duplicate first and last name pairs (with different IDs) allowed in DB)
     * @param newUser user's first name and last name
     * @return added user
     */
    @PostMapping("/users")
    public ResponseEntity<UserDto> addUser(@RequestBody @Valid CreateUserDto newUser) {

        final User user = usersService.save(new User(newUser.getFirstName(), newUser.getLastName()));

        return ResponseEntity.ok(new UserDto(user.getId(), user.getFirstName(), user.getLastName()));
    }

    /**
     * @param updatedUser user's id, first name and last name (requires providing all user's fields)
     * @return updated user
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<UserDto> updateUser(@RequestBody @Valid CreateUserDto updatedUser, @PathVariable Long id) {

        return usersService.update(id, updatedUser)
                .map(user -> ResponseEntity.ok(new UserDto(user.getId(), user.getFirstName(), user.getLastName())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * @param updatedUser user's id, first name and last name (doesn't require providing all the user's fields)
     * @return patched user
     */
    @PatchMapping("/users/{id}")
    public ResponseEntity<UserDto> patchUser(@RequestBody @Valid PatchUserDto updatedUser, @PathVariable Long id) {

        return usersService.patch(id, updatedUser)
                .map(user -> ResponseEntity.ok(new UserDto(user.getId(), user.getFirstName(), user.getLastName())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * @param id id of the user to delete
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {

        final Optional<User> userToDelete = usersService.findById(id);

        if (userToDelete.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        usersService.deleteById(id);

        return ResponseEntity.ok().build();
    }
}
