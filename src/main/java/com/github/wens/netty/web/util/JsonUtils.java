package com.github.wens.netty.web.util;

import cn.hutool.json.JSONUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * @author hgh
 * 19-12-12
 */
public class JsonUtils {
    private static final Logger log = LoggerFactory.getLogger("JsonUtils");

    /**
     * 将对象转换成json字符串。
     */
    public static byte[] serialize(Object data) {
        String s = JSONUtil.toJsonStr(JSONUtil.parseObj(data, false));
        return s.getBytes(Charset.forName("UTF-8"));
    }

    /**
     * 将json结果集转化为对象
     *
     * @param jsonData json数据
     * @param beanType 对象中的object类型
     */
    public static <T> T deserialize(byte[] jsonData, Class<T> beanType) {
        T t = null;
        try {
            t = JSONUtil.toBean(new String(jsonData, "UTF-8"), beanType);
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage());
        }
        return t;
    }
}
