package com.honeywell.net.utils;

import android.text.TextUtils;

import java.net.URLEncoder;

/**
 * 
 * URL 参数拼接类
 * 
 * @author zhujunyu
 */
public class ParamsSplice {

    private StringBuilder params = new StringBuilder();
    private boolean isEncode = true;

    /**
     * 默认构造,默认对参数URLEncode
     */
    public ParamsSplice() {
        this(true);
    }

    /**
     * 构造是否对参数URLEncode
     * @param isEncode 如果true编码 false不编码
     */
    public ParamsSplice(boolean isEncode) {
        this.isEncode = isEncode;
    }

    /**
     * 
     * 添加一个String参数</Br> 包含参数检查,包括key为空,value为空,null,"null","-1"
     * 
     * @param key
     * @param value
     * @return
     */
    public ParamsSplice append(String key, String value) {
        if (TextUtils.isEmpty(key) || TextUtils.isEmpty(value)
                || "null".equalsIgnoreCase(value) || "-1".equals(value)) {
            return this;
        }

        return appendIgnoreCheck(key, value);
    }

    /**
     * 
     * 添加一个Int参数</Br> 包含参数检查,包括key为空,value为-1
     * 
     * @param key
     * @param value
     * @return
     */
    public ParamsSplice append(String key, int value) {
        return append(key, String.valueOf(value));
    }

    /**
     * 
     * 添加一个Long参数</Br> 包含参数检查,包括key为空,value为-1
     * 
     * @param key
     * @param value
     * @return
     */
    public ParamsSplice append(String key, long value) {
        if (value == -1) {
            return this;
        }
        return append(key, String.valueOf(value));
    }

    /**
     * 
     * 添加一个String参数</Br> 忽略参数检查
     * 
     * @param key
     * @param value
     * @return
     */
    public ParamsSplice appendIgnoreCheck(String key, String value) {
        if (params.length() > 0) {
            params.append("&");
        }

        if (isEncode) {
            params.append(URLEncoder.encode(key) + "="
                    + URLEncoder.encode(value));
        } else {
            params.append(key + "=" + value);
        }

        return this;
    }

    /**
     * 
     * 添加一个Int参数</Br> 忽略参数检查
     * 
     * @param key
     * @param value
     * @return
     */
    public ParamsSplice appendIgnoreCheck(String key, int value) {
        return appendIgnoreCheck(key, String.valueOf(value));
    }

    /**
     * 
     * 添加一个Long参数</Br> 忽略参数检查
     * 
     * @param key
     * @param value
     * @return
     */
    public ParamsSplice appendIgnoreCheck(String key, long value) {
        return appendIgnoreCheck(key, String.valueOf(value));
    }

    /**
     * 获得拼接的参数串 like this: </Br> "cid=8888000&gid=710002&..."
     */
    public String toString() {
        return params.toString();
    }

    public ParamsSplice copy() {
        ParamsSplice ps = new ParamsSplice();
        ps.params = new StringBuilder(toString());
        return ps;
    }
}
