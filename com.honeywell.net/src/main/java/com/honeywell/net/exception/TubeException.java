/*
 * FileName:	TubeException.java
 * Author: 		zhujunyu
 * Description:	<文件描述>
 * History:		2013-10-16 1.00 初始版本
 */
package com.honeywell.net.exception;

/**
 *  </Br>
 * <功能详细描述> </Br>
 * 
 * @author  zhujunyu
 */
public class TubeException extends Exception {

    /**
     * 注释内容
     */
    private static final long serialVersionUID = 3005783288029619732L;

    /**
     * 常规错误
     */
    public static final int NORMAL_CODE = -1;
    
    /**
     * 网络异常
     */
    public static final int IO_ERROR_CODE = 1;
    
    /**
     * 数据解析异常
     */
    public static final int DATA_ERROR_CODE = 2;
    
    /**
     * 服务器异常
     */
    public static final int SERVER_ERROR_CODE = 3;
    
    
    private int mCode = NORMAL_CODE;

    public TubeException(String info) {
        this(info, NORMAL_CODE);
    }
    
    public TubeException(String info, int code) {
        super(info);
        mCode = code;
    }
    
    public TubeException(Throwable throwable, int code) {
        super(throwable);
        mCode = code;
    }
    
    /**
     * 获取错误码
     * @return
     */
    public int getCode() {
        return mCode;
    }
}
