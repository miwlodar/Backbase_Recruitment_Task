package backbase.task.db;

import backbase.task.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UsersRepository extends JpaRepository<User, Long> {

    Page<User> findAll(Pageable pageable);

    List<User> findByLastNameContainsAllIgnoreCase(String last_name);

}
