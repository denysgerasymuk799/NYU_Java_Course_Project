package com.unobank.auth_service.security.jwt;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import java.util.Collection;

@Data
public class RestUserDetails {
    private String cardId;

    private String email;

    private Collection<? extends GrantedAuthority> authorities;

    public RestUserDetails(String cardId, String email, Collection<? extends GrantedAuthority> authorities) {
        this.cardId = cardId;
        this.email = email;
        this.authorities = authorities;
    }
}
