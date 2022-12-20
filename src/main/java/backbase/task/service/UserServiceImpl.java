package backbase.task.service;

import backbase.task.db.UsersRepository;
import backbase.task.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UsersRepository usersRepository;

    public UserServiceImpl(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public Page<User> findAll(Pageable pageable) {

        return usersRepository.findAll(pageable);
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(usersRepository
                .findById(id)
                .orElseThrow(() -> new RuntimeException("Did not find user's id - " + id)));
    }

    @Override
    public void save(User user) {
        usersRepository.save(user);
    }

    @Override
    public void deleteById(Long id) {
        usersRepository.deleteById(id);
    }

    @Override
    public List<User> searchByLastName(String lastName) {
        return usersRepository.findByLastNameContainsAllIgnoreCase(lastName);
    }
}
