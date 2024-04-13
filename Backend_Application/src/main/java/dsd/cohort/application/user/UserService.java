package dsd.cohort.application.user;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface UserService {


    Optional<UserEntity> findUserByEmail(String email);

    boolean userExists(String email);

    UserEntity createUser(UserEntity user);
}
