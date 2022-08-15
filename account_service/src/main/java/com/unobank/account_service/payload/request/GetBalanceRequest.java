package com.unobank.account_service.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GetBalanceRequest {
    @NotBlank
    @JsonProperty("card_id")
    @Size(min=16, max=16)
    String cardId;
}
