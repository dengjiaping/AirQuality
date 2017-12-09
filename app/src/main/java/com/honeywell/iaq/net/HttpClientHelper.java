package com.honeywell.iaq.net;

import android.content.Context;
import android.util.Log;

import com.honeywell.iaq.R;
import com.honeywell.iaq.utils.Constants;
import com.honeywell.iaq.utils.Utils;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.MySSLSocketFactory;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import java.security.KeyStore;
import java.util.Map;

import cz.msebera.android.httpclient.conn.scheme.PlainSocketFactory;
import cz.msebera.android.httpclient.conn.scheme.Scheme;
import cz.msebera.android.httpclient.conn.scheme.SchemeRegistry;
import cz.msebera.android.httpclient.conn.ssl.SSLSocketFactory;
import cz.msebera.android.httpclient.entity.ByteArrayEntity;
import cz.msebera.android.httpclient.impl.cookie.BasicClientCookie;

/**
 * Created by H157925 on 16/4/14. 13:28
 * Email:Shodong.Sun@honeywell.com
 */
public class HttpClientHelper {
    private static final String TAG = "HttpClientHelper";
    //单例模式
    private static HttpClientHelper instance;
    private static PersistentCookieStore myCookieStore;

    public static final String NO_COOKIE = "no_cookie";
    public static final String COOKIE = "Cookie";


    /**
     * 定义个异步网络客户端，默认超时10秒，当超过，默认重连次数为5次 默认最大连接数为10个
     */
    private static AsyncHttpClient client = new AsyncHttpClient(getSchemeRegistry());

    static {
        try {
            /// We initialize a default Keystore
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
            socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);


            client.setSSLSocketFactory(socketFactory);
        } catch (Exception e) {
            e.printStackTrace();
        }
        client.setTimeout(10000);//设置超时时间
    }

    public static SchemeRegistry getSchemeRegistry() {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            SSLSocketFactory sf = new MySSLSocketFactory(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            HttpParams params = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(params, 10000);
            HttpConnectionParams.setSoTimeout(params, 10000);
            SchemeRegistry registry = new SchemeRegistry();
            registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
            registry.register(new Scheme("https", sf, 443));
            return registry;
        } catch (Exception e) {
            return null;
        }
    }

    private HttpClientHelper() {

    }

    /**
     * Http Get method, 存在异常或者请求超时的情况下，回调返回值将是空值
     *
     * @param uri      请求的uri
     * @param callback 请求完成后回调方法
     */
//    public void httpGET(Context context, String uri, final AsyncHttpResponseHandler callback) throws UnsupportedEncodingException {
//        httpRequest(context, uri, null, NO_COOKIE, callback, GET);
//    }


    /**
     * Http Post method 存在异常或者请求超时情况下，回调返回值将是空字符串
     *
     * @param uri      请求的uri
     * @param callback 请求完成后回调的方法
     */
//    public void httpPOST(Context context, String uri, Map params, String action, final AsyncHttpResponseHandler callback) throws UnsupportedEncodingException {
//        httpRequest(context, uri, params, action, callback, POST);
//    }

    /**
     * 构建一个单例
     */
    public static HttpClientHelper newInstance() {
        if (null == instance) {
            synchronized (HttpClientHelper.class) {
                if (null == instance) {
                    instance = new HttpClientHelper();
                }
            }
        }
        return instance;
    }

    public void httpRequest(final Context context, final String uri, final Map params, final String action, final AsyncHttpResponseHandler callback, final int type, final String cookie) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                //判断网络状态
                if (Utils.isNetworkAvailable(context)) {
                    if (!Utils.isNetworkConnected(context)) {
                        String str = context.getString(R.string.no_network);
                        callback.sendFailureMessage(40, null, str.getBytes(), null);
                        return;
                    }
                } else {
                    String str = context.getString(R.string.no_network);
                    callback.sendFailureMessage(40, null, str.getBytes(), null);
                    return;
                }

                //得到请求参数
                RequestParams requestParams = new RequestParams();
                if (null != params && params.size() > 0) {
                    for (Object key : params.keySet()) {
                        requestParams.put((String) key, params.get(key));
                    }
                }
//        Log.e(TAG, "http request str :" + requestParams.toString());

                if (myCookieStore == null) {
                    myCookieStore = new PersistentCookieStore(context);
                }
                client.setCookieStore(myCookieStore);

                //设置coockie 需要coockie的时候再设置
                if (!action.equals(NO_COOKIE)) {
                    BasicClientCookie newCookie = new BasicClientCookie(Constants.KEY_COOKIE, cookie);
                    myCookieStore.addCookie(newCookie);
                }

                switch (type) {
                    case GET:
                        client.get(context, uri, requestParams, callback);
                        break;
                    case POST: {
                        //post json
                        JSONObject bodyAsJson = new JSONObject(params);
                        String JsonStr = bodyAsJson.toString();
                        Log.e(TAG, "http request post str :" + JsonStr);
                        try {
                            ByteArrayEntity entity = new ByteArrayEntity(JsonStr.getBytes("UTF-8"));
                            client.post(context, uri, entity, "application/json", callback);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                    break;
                    default:
                        break;
                }
            }
        }).start();
    }

    public void httpPostRequest(final Context context, final String uri, final JSONObject jsonObject, final String action, final AsyncHttpResponseHandler callback, final String cookie) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (Utils.isNetworkAvailable(context)) {
                    if (!Utils.isNetworkConnected(context)) {
                        String str = context.getString(R.string.no_network);
                        callback.sendFailureMessage(40, null, str.getBytes(), null);
                        return;
                    }
                } else {
                    String str = context.getString(R.string.no_network);
                    callback.sendFailureMessage(40, null, str.getBytes(), null);
                    return;
                }

                if (myCookieStore == null) {
                    myCookieStore = new PersistentCookieStore(context);
                }
                client.setCookieStore(myCookieStore);

                if (!action.equals(NO_COOKIE)) {
                    BasicClientCookie newCookie = new BasicClientCookie(Constants.KEY_COOKIE, cookie);
                    myCookieStore.addCookie(newCookie);
                }

                try {
                    String JsonStr = jsonObject.toString();
                    Log.e(TAG, "http request post str :" + JsonStr);
                    ByteArrayEntity entity = new ByteArrayEntity(JsonStr.getBytes("UTF-8"));
                    client.post(context, uri, entity, "application/json", callback);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public enum CResponceHandlerType {
        Text,
        Json
    }

    public static final int GET = 1;
    public static final int POST = 2;
}
