/*
 * FileName:	TubeConfig.java
 * Author: 		zhujunyu
 * Description:	<文件描述>
 * History:		2013-10-24 1.00 初始版本
 */
package com.honeywell.net.core;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * 环境变量，公共配置项
 * 
 * @author zhujunyu
 * @see Builder
 */
public class TubeConfig {

    protected int mThreadCount = 0;

    protected boolean isDebug = false;

    protected Map<String, LinkedList<String>> mHosts = null;

    protected TubeOptions mDefaultOptions;

    protected HashMap<String, String> mCommonHeaders = null;

    private TubeConfig() {
    }

    public static TubeConfig getDefault() {
        return new TubeConfig.Builder().create();
    }
    
    /**
     * 
     * TubeConfig 的建造器
     * 
     * @author zhujunyu
     */
    public static class Builder {

        private int mThreadCount = 0;

        private boolean mIsDebug = false;

        private Map<String, LinkedList<String>> mHosts = null;

        private HashMap<String, String> mHeaders = new HashMap<String, String>();

        private TubeOptions mDefaultOptions = null;

        public Builder() {
            mHosts = new HashMap<String, LinkedList<String>>();
        }

        /**
         * 
         * 设置最高并发线程数，0或不设置为不限制
         * 
         * @param count
         * @return
         */
        public Builder setThreadCount(int count) {
            mThreadCount = count;
            return this;
        }

        /**
         * 
         * 添加可切换的主机列表
         * 
         * @param key
         *            一组主机列表的key
         * @param hosts
         *            主机列表
         * @return
         */
        public Builder addHostList(String key, LinkedList<String> hosts) {
            mHosts.put(key, hosts);
            return this;
        }

        /**
         * 是否为调试模式 已废弃
         * @param isDebug
         * @return
         * @deprecated
         */
        public Builder setDebugable(boolean isDebug) {
            mIsDebug = isDebug;
            return this;
        }

        /**
         * 设置全局公共请求头,如果设置,则所有请求都携带此头部信息
         * @param headers
         * @return
         */
        public Builder setCommonHeaders(HashMap<String, String> headers) {
            mHeaders.putAll(headers);
            return this;
        }

        /**
         * 设置默认的Tube请求选项
         * @param options
         * @return
         */
        public Builder setDefaultOptions(TubeOptions options) {
            mDefaultOptions = options;
            return this;
        }

        /**
         * 创建并返回一个TubeConfig实例
         * @return
         */
        public TubeConfig create() {
            TubeConfig cfg = new TubeConfig();

            if (mDefaultOptions == null) {
                mDefaultOptions = new TubeOptions.Builder().create();
            }

            cfg.mThreadCount = mThreadCount;
            cfg.mHosts = mHosts;
            cfg.isDebug = mIsDebug;
            cfg.mCommonHeaders = mHeaders;
            cfg.mDefaultOptions = mDefaultOptions;

            return cfg;
        }
    }

}
