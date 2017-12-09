/*
 * FileName:	JSONTubeListener.java
 * Author: 		zhujunyu
 * Description:	<文件描述>
 * History:		2013-10-16 1.00 初始版本
 */
package com.honeywell.net.listener;

import org.json.JSONObject;

/**
 * 
 * Http请求返回类型是JSONObject的接口
 * 
 * @author zhujunyu
 */
public interface JSONTubeListener<Result> extends TubeListener<JSONObject, Result> {

}
