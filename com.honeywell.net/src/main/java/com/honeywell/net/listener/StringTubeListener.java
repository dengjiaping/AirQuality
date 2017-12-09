/*
 * FileName:	StringTubeListener.java
 * Author: 		zhujunyu
 * Description:	<文件描述>
 * History:		2013-10-16 1.00 初始版本
 */
package com.honeywell.net.listener;

/**
 * 
 * Http请求返回类型是String的接口
 * 
 * @author zhujunyu
 */
public interface StringTubeListener<Result> extends
        TubeListener<String, Result> {

}
