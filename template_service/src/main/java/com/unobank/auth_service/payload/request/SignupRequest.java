package com.unobank.auth_service.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.Set;

@Data
public class SignupRequest {
    @NotBlank
    @Size(min = 3, max = 20)
    private String username;
 
    @NotBlank
    @Size(max = 50)
    @Email
    private String email;
    
    private Set<String> roles;
    
    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

    @NotBlank
    @JsonProperty("firstname")
    @Size(min = 2, max = 100)
    private String firstName;

    @NotBlank
    @JsonProperty("lastname")
    @Size(min = 2, max = 100)
    private String lastName;
}
