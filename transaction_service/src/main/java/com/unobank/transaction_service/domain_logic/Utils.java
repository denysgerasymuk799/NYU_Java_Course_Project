package com.unobank.transaction_service.domain_logic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.unobank.transaction_service.domain_logic.enums.Events;
import com.unobank.transaction_service.dto.ProcessingTransactionMessage;
import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class Utils {
    private final static ObjectMapper objectMapper = new ObjectMapper();
    public static boolean isNumeric(String numericStr) {
        if (numericStr == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(numericStr);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static String getFormattedDate(Date date) {
        String pattern = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(date);
    }

    public static boolean isResult(String value) {
        try {
            ProcessingTransactionMessage transaction = objectMapper.readValue(value, ProcessingTransactionMessage.class);
            return (transaction.getEventName().equals(Events.TRANSACTION_SUCCESS.label)) ||
                    (transaction.getEventName().equals(Events.TRANSACTION_FAILURE.label)) ||
                    (transaction.getEventName().equals(Events.RESERVATION_FAILURE.label));
        } catch (Exception e) {
            log.error(e.toString());
            return false;
        }
    }

    public static boolean isProcessingTransaction(String value) {
        try {
            ProcessingTransactionMessage transaction = objectMapper.readValue(value, ProcessingTransactionMessage.class);
            return (transaction.getEventName().equals(Events.TRANSACTION_TOPUP.label)) ||
                    (transaction.getEventName().equals(Events.TRANSACTION_REQUEST.label)) ||
                    (transaction.getEventName().equals(Events.RESERVATION_SUCCESS.label));
        } catch (Exception e) {
            log.error(e.toString());
            return false;
        }
    }
}
