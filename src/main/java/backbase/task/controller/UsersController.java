package backbase.task.controller;

import backbase.task.dto.CreateUserDto;
import backbase.task.dto.GetUserDto;
import backbase.task.entity.User;
import backbase.task.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

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

    // exposing "/users" endpoint and returning list of all users (with Pageable interface - pagination and sorting)
    // including users' IDs in returned JSON - needed for updating and deleting users
    @GetMapping("/users")
    public Page<User> findAll(Pageable pageable) {

        return userService.findAll(pageable);
    }

    //mapping for 1st required endpoint (GET list of users with given last name) - with duplicates removed
    @GetMapping("/users/{lastName}")
    public List<GetUserDto> getUsersByLastName(@PathVariable String lastName) {

        final List<User> foundUsers = userService.searchByLastName(lastName);

        if (foundUsers == null) {
            throw new RuntimeException("No users found with last name: " + lastName);
        }

        return foundUsers.stream()
                .map(user -> new GetUserDto(user.getFirstName(), user.getLastName()))
                .distinct()
                .collect(Collectors.toList());
    }

    //mapping for 2nd required endpoint (GET list of names only for the given last name) - with duplicates removed
    @GetMapping("/users/first-names/{lastName}")
    public List<String> getFirstNamesByLastName(@PathVariable String lastName) {

        final List<User> foundUsers = userService.searchByLastName(lastName);

        if (foundUsers == null) {
            throw new RuntimeException("No users found with last name: " + lastName);
        }

        return foundUsers.stream()
                .map(User::getFirstName)
                .distinct()
                .collect(Collectors.toList());
    }

    // mapping for 3rd required endpoint (POST - adding a new user) - allowing duplicate records in DB (with different IDs)
    @PostMapping("/users")
    public CreateUserDto addUser(@RequestBody CreateUserDto newUser) {

        userService.save(new User(newUser.getFirstName(), newUser.getLastName()));

        return newUser;
    }

    // mapping for PUT /users/{id} - update existing user (requires providing all the user's fields)
    @PutMapping("/users/{id}")
    public CreateUserDto updateUser(@RequestBody CreateUserDto updatedUser, @PathVariable Long id) {

        final Optional<User> userToUpdate = userService.findById(id);

        // throwing exception if the user's not found (empty Optional)
        if (userToUpdate.isEmpty()) {
            throw new RuntimeException("User's id not found: " + userToUpdate);
        }

        userService.save(new User(id, updatedUser.getFirstName(), updatedUser.getLastName()));

        return updatedUser;
    }

    // mapping for PATCH /users/{id} - update existing user (doesn't require providing all the user's fields)
    @PatchMapping("/users/{id}")
    public CreateUserDto patchUser(@RequestBody CreateUserDto updatedUser, @PathVariable Long id) {

        final Optional<User> userToUpdate = userService.findById(id);

        // throwing exception if the user's not found (empty Optional)
        if (userToUpdate.isEmpty()) {
            throw new RuntimeException("User's id not found: " + userToUpdate);
        }

        if (updatedUser.getFirstName() != null && updatedUser.getLastName() != null) {
            userService.save(new User(id, updatedUser.getFirstName(), updatedUser.getLastName()));
        } else if (updatedUser.getFirstName() != null) {
            userService.save(new User(id, updatedUser.getFirstName(), userToUpdate.get().getLastName()));
            updatedUser.setLastName(userToUpdate.get().getLastName());
        } else {
            userService.save(new User(id, userToUpdate.get().getFirstName(), updatedUser.getLastName()));
            updatedUser.setFirstName(userToUpdate.get().getFirstName());
        }

        return updatedUser;
    }

    // mapping for DELETE /users/{id}
    @DeleteMapping("/users/{id}")
    public String deleteUser(@PathVariable Long id) {

        final Optional<User> userToDelete = userService.findById(id);

        // throwing exception if the user's not found (empty Optional)
        if (userToDelete.isEmpty()) {
            throw new RuntimeException("User's id not found: " + userToDelete);
        }

        userService.deleteById(id);

        return "Deleted user's id: " + id;
    }
}
