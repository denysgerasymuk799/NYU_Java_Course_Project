package com.unobank.auth_service.database.models;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Document(collection = "registration_cards")
public class Card {
  @Id
  private String id;

  @NotBlank
  @Size(min=16, max=16)
  @Field(name = "card_id")
  private String cardId;

  @NotBlank
  @Size(min=3, max=3)
  private String cvv;

  private boolean enabled;

  @NotBlank
  @Size(min=5, max=5)
  @Field(name = "expiration_date")
  private String expirationDate;

  public Card() {
  }

  public Card(String cardId, String cvv, boolean enabled, String expirationDate) {
    this.cardId = cardId;
    this.cvv = cvv;
    this.enabled = enabled;
    this.expirationDate = expirationDate;
  }
}
