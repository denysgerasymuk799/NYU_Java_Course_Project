package com.unobank.card_service.domain_logic;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

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

    public static Date convertLocalDateToDate(LocalDate localDate) {
        if (localDate == null)
            return null;
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}
