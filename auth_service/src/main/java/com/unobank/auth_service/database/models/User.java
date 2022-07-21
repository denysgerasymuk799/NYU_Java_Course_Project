package com.unobank.auth_service.database.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Size;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


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
    private String address;
    private Date birthdayDate;
    private String city;
    private Boolean disabled;
    private String email;
    @Size(min = 2, message = "firstName length must be > 1")
    private String firstName;
    @Size(min = 2, message = "lastName length must be > 1")
    private String lastName;
    private String hashedPassword;
    private String cardId;

    public User() {

    }

    public User(String address, String birthdayDate, String city, String email,
                String firstName, String lastName, String hashedPassword, String cardId) throws ParseException {
        this.address = address;
        this.city = city;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.hashedPassword = hashedPassword;
        this.cardId = cardId;

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        this.birthdayDate = formatter.parse(birthdayDate);
    }
}
