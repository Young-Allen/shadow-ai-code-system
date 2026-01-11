package com.shadow.aicodingsystem.common;

import com.shadow.aicodingsystem.exception.ErrorCode;

public class ResultUtils {
    /**
     * 成功返回结果
     * @param data 获取的数据
     * @param <T> 泛型
     * @return Result<T>
     */
    public static <T> BaseResponse<T> success(T data){
        return new BaseResponse<>(0, data, "ok");
    }

    /**
     * 失败返回结果
     * @param errorCode 错误码
     * @return Result<T>
     */
    public static BaseResponse<?> error(ErrorCode errorCode){
        return new BaseResponse<>(errorCode);
    }

    /**
     * 失败返回结果
     * @param errorCode 错误码
     * @param message 错误信息
     * @return Result<T>
     */
    public static BaseResponse<?> error(ErrorCode errorCode, String message){
        return new BaseResponse<>(errorCode.getCode(), null, message);
    }

    /**
     * 失败返回结果
     * @param message 错误信息
     * @code 错误码
     * @return Result<T>
     */
    public static BaseResponse<?> error(int code, String message){
        return new BaseResponse<>(code, null, message);
    }

}
