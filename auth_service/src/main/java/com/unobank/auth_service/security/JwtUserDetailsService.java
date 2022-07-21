package com.unobank.auth_service.security;

import lombok.extern.slf4j.Slf4j;
import com.unobank.auth_service.database.models.User;
import com.unobank.auth_service.security.jwt.JwtUser;
import com.unobank.auth_service.security.jwt.JwtUserFactory;
import com.unobank.auth_service.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implementation of {@link UserDetailsService} interface for {@link JwtUser}.
 *
 * @author Eugene Suleimanov
 * @version 1.0
 */

@Service
@Slf4j
public class JwtUserDetailsService {

    private final UserService userService;

    @Autowired
    public JwtUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    public Mono<UserDetails> loadUserByUsername(String email) {
        return userService.findByEmail(email)
                .flatMap(user -> {
                    if (user.getEmail().length() > 0) {
                        JwtUser jwtUser = JwtUserFactory.create(user);
                        log.info("IN loadUserByUsername - user with email: {} successfully loaded", email);
                        return Mono.just(jwtUser);
                    } else {
                        return Mono.empty();
                    }
                });
    }
}
