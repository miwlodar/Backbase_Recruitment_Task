//interface for User service - all CRUD methods, implemented in a separate class
package backbase.task.service;

import backbase.task.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserService {

    Page<User> findAll(Pageable pageable);

    Optional<User> findById(Long id);

    void save(User user);

    void deleteById(Long id);

    List<User> searchByLastName(String lastName);
}
