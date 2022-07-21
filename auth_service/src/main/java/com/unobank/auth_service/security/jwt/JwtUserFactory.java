package com.unobank.auth_service.security.jwt;

import com.unobank.auth_service.database.models.Role;
import com.unobank.auth_service.database.models.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of Factory Method for class {@link JwtUser}.
 *
 * @author Eugene Suleimanov
 * @version 1.0
 */

public final class JwtUserFactory {

    public JwtUserFactory() {
    }

    public static JwtUser create(User user) {
        ArrayList<Role> roles = new ArrayList<>();
        roles.add(user.getRole());
        return new JwtUser(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getHashedPassword(),
                mapToGrantedAuthorities(roles),
                false
        );
    }

    private static List<GrantedAuthority> mapToGrantedAuthorities(List<Role> userRoles) {
        return userRoles.stream()
                .map(role ->
                        new SimpleGrantedAuthority(role.toString())
                ).collect(Collectors.toList());
    }
}
