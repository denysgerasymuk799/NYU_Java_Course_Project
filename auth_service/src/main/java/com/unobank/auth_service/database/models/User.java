package com.unobank.auth_service.database.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Size;
import java.text.ParseException;


/**
 * Domain object that represents an application user.
 *
 * @version 1.0
 */

@Data
@Document("auth_users")
public class User {

    @Id
    private String id;
    private Boolean disabled;
    private String email;
    private String password;
    @Size(min = 2, message = "firstName length must be > 1")
    private String firstName;
    @Size(min = 2, message = "lastName length must be > 1")
    private String lastName;
    private String hashedPassword;
    private String cardId;
    private Role role;

    public User() {

    }

    public User(String email, String firstName, String lastName, String hashedPassword, String cardId, Role role) throws ParseException {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.hashedPassword = hashedPassword;
        this.cardId = cardId;
        this.disabled = false;
        this.role = role;
    }
}
