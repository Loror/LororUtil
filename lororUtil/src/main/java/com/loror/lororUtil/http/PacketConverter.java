package com.loror.lororUtil.http;

import java.util.Map;

//打包转换器
public interface PacketConverter {
    String convert(String method, Map<String, Object> params);
}
