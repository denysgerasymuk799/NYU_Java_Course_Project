package com.unobank.orchestrator_service.domain_logic;

import com.unobank.orchestrator_service.payload.request.TransactionRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.ArrayList;
import java.util.Set;

@Slf4j
@Component
public class CustomValidator<T> {
    @Autowired
    private Validator validator;

    public ArrayList<String> validateRequest(T object) {
        ArrayList<String> violationMessages = new ArrayList<>();
        Set<ConstraintViolation<T>> violations = validator.validate(object);
        for (ConstraintViolation<T> violation : violations) {
            String errMessage = violation.getMessage();
            violationMessages.add(errMessage);
            log.error(errMessage);
        }
        return violationMessages;
    }
}
