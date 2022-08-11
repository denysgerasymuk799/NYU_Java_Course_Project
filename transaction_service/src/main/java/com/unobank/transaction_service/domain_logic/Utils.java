package com.unobank.transaction_service.domain_logic;

public class Utils {
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
