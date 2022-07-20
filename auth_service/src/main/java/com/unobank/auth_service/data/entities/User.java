package com.unobank.auth_service.data.entities;

import com.unobank.auth_service.controllers.AuthController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import javax.validation.constraints.Size;

@Document("registered_users")
public class User {
    private static final Logger LOGGER = LoggerFactory.getLogger(User.class);
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

    public User(String address, String birthdayDate, String city, String email, String firstName, String lastName, String hashedPassword, String cardId) throws ParseException {
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

    public String getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getBirthdayDate() {
        return birthdayDate;
    }

    public void setBirthdayDate(Date birthdayDate) {
        this.birthdayDate = birthdayDate;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}
