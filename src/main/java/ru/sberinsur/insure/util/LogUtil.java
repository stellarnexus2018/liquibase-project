package ru.sberinsur.insure.util;

import org.slf4j.MDC;

public class LogUtil {
    public static void setApplicationNum(long applicationNum) {
        MDC.put("applicationNum", String.valueOf(applicationNum));
    }

    public static void setUserLogin(String userLogin) {
        MDC.put("userLogin", userLogin);
    }

    public static void clearContext() {
        MDC.clear();
    }
}
