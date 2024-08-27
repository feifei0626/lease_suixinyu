package com.atguigu.lease.common.utils;

import java.util.Random;

//随机生成验证码

public class VerifyCodeUtil {
    public static String getVerifyCode(int length) {
        StringBuilder builder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            builder.append(random.nextInt(10));  //随机生成0-9的数字
        }
        return builder.toString();
    }
}
