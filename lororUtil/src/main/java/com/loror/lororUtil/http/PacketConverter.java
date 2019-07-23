package com.loror.lororUtil.http;

import java.util.HashMap;

//打包转换器
public interface PacketConverter {
    String convert(String method, HashMap<String, Object> params);
}
