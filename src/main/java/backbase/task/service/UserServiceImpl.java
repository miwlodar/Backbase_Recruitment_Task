package backbase.task.service;

import backbase.task.db.UsersRepository;
import backbase.task.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UsersRepository usersRepository;

    public UserServiceImpl(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public List<User> findAll() {
        return usersRepository.findAll();
    }

    @Override
    public void save(User user) {
        usersRepository.save(user);
    }

    @Override
    public void deleteById(Long id) {
        usersRepository.deleteById(id);
    }
}
