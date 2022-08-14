package com.unobank.orchestrator_service.domain_logic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.unobank.orchestrator_service.domain_logic.enums.Events;
import com.unobank.orchestrator_service.domain_logic.enums.TransactionType;
import com.unobank.orchestrator_service.domain_logic.kafka.ServiceKafkaProducer;
import com.unobank.orchestrator_service.dto.TransactionMessage;
import com.unobank.orchestrator_service.payload.request.TransactionRequest;
import com.unobank.orchestrator_service.security.jwt.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.UUID;

@Slf4j
@Component
public class TransactionHandler {
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private CustomValidator<TransactionRequest> validator;

    @Autowired
    private ServiceKafkaProducer producer;

    public String handleTransaction(String jwt, TransactionRequest transactionRequest, String transactionId) throws JsonProcessingException {
        String username = jwtUtils.getUserNameFromJwtToken(jwt);
        log.info("User {} is making a new transaction", username);

        Claims jwtClaims = jwtUtils.getAllClaimsFromToken(jwt);
        LinkedHashMap<String, String> userDetails = jwtClaims.get("user_details", LinkedHashMap.class);

        ArrayList<String> violationMessages = validator.validateRequest(transactionRequest);
        if (violationMessages.size() != 0) {
            String errMessages = String.join("  \n", violationMessages);
            return "Input fields are in an incorrect format. Error messages: " + errMessages;
        }

        if (! userDetails.get("cardId").equals(transactionRequest.getSenderCardId())) {
            return "Authorized user card id is not equal to sender card id in the request body.";
        }

        System.out.println("transactionRequest: " + transactionRequest);
        System.out.println("transactionRequest amount: " + transactionRequest.getAmount());

        String eventName;
        if (transactionRequest.getTransactionType().equals(TransactionType.TOP_UP)) {
            eventName = Events.TRANSACTION_TOPUP.label;
        } else {
            eventName = Events.TRANSACTION_REQUEST.label;
        }
        
        producer.processTransaction(new TransactionMessage(eventName, Constants.MESSAGE_TYPE_REQUEST, Constants.RESPONSE_SUCCESS,
                "", transactionId, transactionRequest));
        return null;
    }
}
