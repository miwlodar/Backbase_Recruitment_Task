//interface for User service - all CRUD methods, implemented in a separate class
package backbase.task.service;

import backbase.task.entity.User;

import java.util.List;

public interface UserService {

    List<User> findAll();

    void save(User user);

    void deleteById(Long id);
}
