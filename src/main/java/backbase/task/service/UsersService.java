package backbase.task.service;

import backbase.task.db.UsersRepository;
import backbase.task.dto.CreateUserDto;
import backbase.task.dto.PatchUserDto;
import backbase.task.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsersService {

    private final UsersRepository usersRepository;

    public UsersService(UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public Page<User> findAll(Pageable pageable) {
        return usersRepository.findAll(pageable);
    }

    public Optional<User> findById(long id) {
        return usersRepository.findById(id);
    }

    public User save(User user) {
        return usersRepository.save(user);
    }

    public Optional<User> update(long id, CreateUserDto updateUserDto) {
        return findById(id).map(user -> {
            user.setFirstName(updateUserDto.getFirstName());
            user.setLastName(updateUserDto.getLastName());
            return usersRepository.save(user);
        });
    }

    public Optional<User> patch(long id, PatchUserDto patchUserDto) {
        return findById(id).map(user -> {
            if (patchUserDto.getFirstName() != null) {
                user.setFirstName(patchUserDto.getFirstName());
            }
            if (patchUserDto.getLastName() != null) {
                user.setLastName(patchUserDto.getLastName());
            }
            return usersRepository.save(user);
        });
    }

    public void deleteById(long id) {
        usersRepository.deleteById(id);
    }

    public List<User> findByLastName(String lastName) {
        return usersRepository.findByLastNameIgnoreCase(lastName);
    }
}
