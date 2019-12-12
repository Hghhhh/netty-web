package com.github.wens.netty.web.util;

import com.alibaba.fastjson.JSON;

/**
 * @author hgh
 * 19-12-12
 */
public class JsonUtils {


    /**
     * 将对象转换成json字符串。
     */
    public static byte[] serialize(Object data) {
        String string = JSON.toJSONString(data);
        return string.getBytes();
    }

    /**
     * 将json结果集转化为对象
     *
     * @param jsonData json数据
     * @param beanType 对象中的object类型
     */
    public static <T> T deserialize(String jsonData, Class<T> beanType) {
        T t = JSON.parseObject(jsonData, beanType);
        return t;
    }
}
