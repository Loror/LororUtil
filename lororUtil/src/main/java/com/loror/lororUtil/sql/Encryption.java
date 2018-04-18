package com.loror.lororUtil.sql;

public interface Encryption {
    /**
     * 加密
     */
    String encrypt(String value);

    /**
     * 解密
     */
    String decrypt(String value);
}
