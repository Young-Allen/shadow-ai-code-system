package com.shadow.aicodingsystem.exception;

public class ThrowUtils {
    /**
     * 条件成立则抛异常
     * @param condition
     * @param exception
     */
    public static void throwIf(boolean condition, RuntimeException exception) {
        if (condition) {
            throw exception;
        }
    }

    /**
    * 条件成立则抛异常
    * @param condition
    * @param errorCode
    */
    public static void throwIf(boolean condition, ErrorCode errorCode) {
        throwIf(condition, new BusinessException(errorCode));
    }


    /**
     * 条件成立则抛异常
     * @param condition
     * @param errorCode
     * @param message
     */
    public static void throwIf(boolean condition, ErrorCode errorCode, String message) {
        throwIf(condition, new BusinessException(errorCode, message));
    }

}
