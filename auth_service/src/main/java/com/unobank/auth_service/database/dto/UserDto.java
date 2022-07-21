package com.unobank.auth_service.database.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import javax.validation.constraints.Size;
import lombok.Data;

import com.unobank.auth_service.database.models.User;


/**
 * DTO class for user requests by ROLE_USER
 *
 * @author Eugene Suleimanov
 * @version 1.0
 */

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDto {
    private String id;
    private Boolean disabled;
    private String email;
    @Size(min = 2, message = "firstName length must be > 1")
    private String firstName;
    @Size(min = 2, message = "lastName length must be > 1")
    private String lastName;
    private String cardId;

    public User toUser(){
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setCardId(cardId);
        user.setDisabled(disabled);

        return user;
    }

    public static UserDto fromUser(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setEmail(user.getEmail());
        userDto.setFirstName(user.getFirstName());
        userDto.setLastName(user.getLastName());
        userDto.setCardId(user.getCardId());
        userDto.setDisabled(user.getDisabled());

        return userDto;
    }
}
