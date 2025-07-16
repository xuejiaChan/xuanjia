package com.xuanjia.smartInterview.constant;

import java.text.Format;

public interface RedisConstant {

    /**
     * 拼接 Redisson 前缀
     */
    String USER_SIGN_IN_REDIS_KEY = "user:signins";

    /**
     * 拼接字段
     * @param year
     * @param userId
     * @return
     */
    static String getUserSignInRedisKey(Integer year, Long userId){
        return String.format("%s:%s:%s",USER_SIGN_IN_REDIS_KEY,year,userId);
    }
}


