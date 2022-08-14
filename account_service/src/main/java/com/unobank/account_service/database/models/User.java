package com.unobank.account_service.database.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

@Data
@Document(collection = "users")
public class User {
  @Id
  private String id;

  @NotBlank
  @Size(min = 2, max = 20)
  private String username;

  @NotBlank
  @Size(min = 2, max = 50)
  @Email
  private String email;

  @NotBlank
  @Size(min = 2, max = 120)
  private String password;

  @NotBlank
  @Size(min = 2, max = 100)
  private String firstName;

  @NotBlank
  @Size(min = 2, max = 100)
  private String lastName;

  private Status status;

  @DBRef
  private Set<Role> roles = new HashSet<>();

  public User() {
  }

  public User(String username, String email, String password, String firstName, String lastName) {
    this.username = username;
    this.email = email;
    this.password = password;
    this.firstName = firstName;
    this.lastName = lastName;
    this.status = Status.ACTIVE;
  }
}
