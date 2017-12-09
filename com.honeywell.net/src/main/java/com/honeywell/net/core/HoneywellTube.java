/*
 * FileName:	EgameBox.java
 * Author: 		zhujunyu
 * Description:	<文件描述>
 * History:		2013-10-16 1.00 初始版本
 */
package com.honeywell.net.core;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

import com.honeywell.net.core.HttpConnector.EntityResult;
import com.honeywell.net.exception.TubeException;
import com.honeywell.net.listener.JSONTubeListener;
import com.honeywell.net.listener.StreamTubeListener;
import com.honeywell.net.listener.StringTubeListener;
import com.honeywell.net.listener.TubeListener;
import com.honeywell.net.utils.Logger;

/**
 * 网络管道类 对于配置信息相同仅URL不同的请求可共用一个对象
 *
 * @author zhujunyu
 */
public class HoneywellTube {

    private TubeThreadPool mTubePool = null;
    private TubeConfig mConfig = TubeConfig.getDefault();

    public HoneywellTube() {
    }

    public void init(TubeConfig cfg) {

        mConfig = cfg;

        if (mTubePool != null) {
            return;
        }

        if (mConfig.mThreadCount > 0) {
            mTubePool = TubeThreadPool.create(mConfig.mThreadCount);
        }

        // Logger.IS_DEBUG_MODE = mConfig.isDebug;
    }

    public void addHosts(String key, LinkedList<String> hosts) {
        if (mConfig != null) {
            mConfig.mHosts.put(key, hosts);
        }
    }

    public void putCommonHeader(String key, String value) {
        if (mConfig != null) {
            mConfig.mCommonHeaders.put(key, value);
        }
    }

    public void release() {
        if (mTubePool != null) {
            new Thread() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    mTubePool.closePool();
                }

            }.start();
        }

        mConfig = null;
    }


    public void get(final String url, final TubeOptions opt,
                    final TubeListener<?, ?> listener) {
        if (listener == null) {
            throw new IllegalArgumentException("The listener can not be null.");
        }

        try {
            new URL(url);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            throw new IllegalArgumentException(
                    "The url can not be parsed. Please check it again.");
        }

        Looper myLooper = Looper.myLooper();

        // 如果本线程没有looper则使用主线程looper 有风险 wei.han 20131031
        if (myLooper == null) {
            myLooper = Looper.getMainLooper();
        }

        if (mTubePool == null) {
            new Thread(getRunnable(myLooper, url, opt, listener), "HoneywellTube:"
                    + hashCode()).start();
        } else {
            mTubePool.execute(getRunnable(myLooper, url, opt, listener));
        }
    }

    public String connect(final String url, final TubeOptions opt) {
        return connectString(url, opt);
    }

    public String connectString(final String url, final TubeOptions opt) {
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            throw new IllegalArgumentException(
                    "The url can not be parsed. Please check it again.");
        }

        return getString(null, url, opt, null);
    }

    public EntityResult connectStream(final String url, final TubeOptions opt) {
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            throw new IllegalArgumentException(
                    "The url can not be parsed. Please check it again.");
        }

        return getStream(null, url, opt, null);
    }

    private Runnable getRunnable(final Looper myLooper, final String url,
                                 final TubeOptions opt, final TubeListener<?, ?> listener) {
        return new Runnable() {

            @SuppressWarnings("unchecked")
            @Override
            public void run() {
                // TODO Auto-generated method stub
                if (listener instanceof StringTubeListener) {
                    getString(myLooper, url, opt,
                            (StringTubeListener<Object>) listener);
                } else if (listener instanceof JSONTubeListener) {
                    getJSON(myLooper, url, opt,
                            (JSONTubeListener<Object>) listener);
                } else if (listener instanceof StreamTubeListener) {
                    getStream(myLooper, url, opt,
                            (StreamTubeListener<Object>) listener);
                }
            }
        };
    }

    private String getString(Looper myLooper, String url, TubeOptions opt,
                             final StringTubeListener<Object> listener) {
        String result = null;

        Handler handler = null;

        // 这种条件说明是异步请求，需要异步返回结果
        if (myLooper != null && listener != null) {
            handler = new Handler(myLooper);
        }

        try {
            EntityResult er = HttpConnector.execute(url, mConfig, opt);
            result = er.entity2String();
            Logger.e("Honeywell Tube","message"+result);
            er.close();
        } catch (TubeException e) {
            // TODO Auto-generated catch block
            if (listener != null) {
                makeFailed(handler, listener, e);
            }

            return null;
        }
//接口不规范，返回空的值 表示成功《修改设备名称》
//        if (TextUtils.isEmpty(result)) {
//            if (listener != null) {
//                makeFailed(handler, listener, new TubeException(
//                        "The result is null or empty."));
//            }
//
//            return null;
//        }

        if (handler != null) {
            try {
                final Object object = listener.doInBackground(result);

                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        listener.onSuccess(object);
                    }
                });
            } catch (Exception e) {
                makeFailed(handler, listener, new TubeException(e,
                        TubeException.DATA_ERROR_CODE));
            }
        }

        return result;
    }

    private void getJSON(Looper myLooper, String url, TubeOptions opt,
                         final JSONTubeListener<Object> listener) {
        JSONObject result = null;

        Handler handler = new Handler(myLooper);

        EntityResult er = null;

        try {
            er = HttpConnector.execute(url, mConfig, opt);
            result = new JSONObject(er.entity2String());
            HttpResponse httpResponse = er.getHttpResponce();

            Header[] headers = httpResponse.getAllHeaders();
            for (int i = 0; i < headers.length; i++) {

                if (headers[i].getName().equals("Set-Cookie")) {
                    String value = headers[i].getValue();
                    result.accumulate("Cookie", value);
                }
            }

            er.close();
        } catch (TubeException e) {
            // TODO Auto-generated catch block
            makeFailed(handler, listener, e);
            return;
        } catch (JSONException e) {
            makeFailed(handler, listener,
                    new TubeException(e.getLocalizedMessage(),
                            TubeException.DATA_ERROR_CODE));
            if (er != null) {
                er.close();
            }
            return;
        }

        try {
            final Object object = listener.doInBackground(result);
            handler.post(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    Logger.e("7777","7777");
                    listener.onSuccess(object);
                }
            });
        } catch (Exception e) {
            makeFailed(handler, listener,
                    new TubeException(e.getLocalizedMessage(),
                            TubeException.DATA_ERROR_CODE));
            return;
        }
    }

    private EntityResult getStream(Looper myLooper, String url,
                                   TubeOptions opt, final StreamTubeListener<Object> listener) {
        EntityResult result = null;

        Handler handler = null;

        // 这种条件说明是异步请求，需要异步返回结果
        if (myLooper != null && listener != null) {
            handler = new Handler(myLooper);
        }

        try {
            result = HttpConnector.execute(url, mConfig, opt);
        } catch (TubeException e) {
            // TODO Auto-generated catch block
            if (listener != null) {
                makeFailed(handler, listener, e);
            }

            return null;
        }

        if (result == null) {
            if (listener != null) {
                makeFailed(handler, listener, new TubeException(
                        "The result is null or empty."));
            }

            return null;
        }

        if (handler != null) {
            try {
                final Object object = listener.doInBackground(result
                        .entity2Stream());

                handler.post(new Runnable() {

                    @Override
                    public void run() {
                        // TODO Auto-generated method stub
                        listener.onSuccess(object);
                    }
                });
            } catch (Exception e) {
                makeFailed(handler, listener, new TubeException(e,
                        TubeException.DATA_ERROR_CODE));
            } finally {
                result.close();
            }
        }

        return result;
    }

    private void makeFailed(Handler handler, final TubeListener<?, ?> listener,
                            final TubeException e) {
        if (handler == null || listener == null) {
            return;
        }

        handler.post(new Runnable() {

            @Override
            public void run() {
                // TODO Auto-generated method stub
                listener.onFailed(e);
            }
        });
    }
}
