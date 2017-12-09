/*
 * FileName:	TubeListener.java
 * Author: 		zhujunyu
 * Description:	<文件描述>
 * History:		2013-10-16 1.00 初始版本
 */
package com.honeywell.net.listener;


import com.honeywell.net.exception.TubeException;

/**
 * 
 * 一般的请求头接口
 * 
 * @author zhujunyu
 */
public interface TubeListener<Params, Result> {

    /**
     * 
     * 请求成功后将得到的数据以指定的形式返回给调用者处理
     * 
     * @param water
     * @return
     */
    public Result doInBackground(Params water) throws Exception;

    /**
     * 
     * 数据处理后返回，返回的线程是构造时的所在线程
     * 
     * @param result
     */
    public void onSuccess(Result result);

    /**
     * 
     * 
     * @param e
     */
    public void onFailed(TubeException e);
}
