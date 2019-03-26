package com.loror.lororUtil.example;

import com.loror.lororUtil.sql.Encryption;

import java.net.URLDecoder;
import java.net.URLEncoder;

public class Code implements Encryption {

    @Override
    public String encrypt(String value) {
        return URLEncoder.encode(value);
    }

    @Override
    public String decrypt(String value) {
        return URLDecoder.decode(value);
    }
}
