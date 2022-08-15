package com.unobank.account_service.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
public class GetTransactionsRequest {
    @NotBlank
    @JsonProperty("card_id")
    @Size(min=16, max=16)
    String cardId;

    @NotBlank
    @JsonProperty("start_idx")
    @Min(0)
    int startIdx;
}
