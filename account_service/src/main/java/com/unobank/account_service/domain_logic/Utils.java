package com.unobank.account_service.domain_logic;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

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
}
