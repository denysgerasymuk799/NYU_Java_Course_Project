package com.unobank.orchestrator_service.domain_logic;

public class Utils {
    public static String getFilenameFromPath(String path) {
        String delimiter = "/";
        String[] split1 = path.split(delimiter);
        return split1[split1.length - 1].split("\\.")[0];
    }
}
