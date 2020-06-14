package me.danieluss.tournaments.security;

import me.danieluss.tournaments.data.model.AppUser;
import me.danieluss.tournaments.data.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Primary
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository repository;

    @Autowired
    public UserDetailsServiceImpl(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        AppUser user = repository.findByEmailIgnoreCase(username);
        if (user == null) {
            throw new RuntimeException(String.format("User, identified by '%s', not found", username));
        }
        return new User(
                user.getEmail(), user.getPassword(),
                AuthorityUtils.createAuthorityList("ROLE_USER"));
    }
}
