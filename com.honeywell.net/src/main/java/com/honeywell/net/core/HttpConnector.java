/*
 * FileName:	HttpConnector.java
 * Author: 		zhujunyu
 * Description:	<文件描述>
 * History:		2013-10-16 1.00 初始版本
 */
package com.honeywell.net.core;

import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnRouteParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyStore;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import com.honeywell.net.exception.TubeException;
import com.honeywell.net.utils.ByteArrayBuilder;
import com.honeywell.net.utils.CookieStoreUtils;
import com.honeywell.net.utils.Logger;

/**
 * </Br> <功能详细描述> </Br>
 *
 * @author zhujunyu
 */
public class HttpConnector {

    public static final int SO_TIMEOUT = 15 * 1000;

    public static final int CONN_TIMEOUT = 15 * 1000;

    public static final int RECONN_TIMES = 4;

    private static final int RECONN_INTERVAL = 2 * 1000;

    private static final String TAG = "TUBE";

    private static Hashtable<String, Integer> sIndexMap = new Hashtable<String, Integer>();

    /**
     * 获取请求数据
     *
     * @param url 请求地址
     * @param cfg 全局配置项
     * @param opt 本次请求参数选项
     * @return
     * @throws TubeException
     */
    public static EntityResult execute(String url, TubeConfig cfg,
                                       TubeOptions opt) throws TubeException {
        if (cfg == null) {
            throw new TubeException("The cfg is null.");
        }

        if (opt == null) {
            opt = cfg.mDefaultOptions;
        }

        if (opt.mMapHeaders == null) {
            opt.mMapHeaders = cfg.mCommonHeaders;
        } else {
            opt.mMapHeaders.putAll(cfg.mCommonHeaders);
        }

        return execute(url, opt, cfg.mHosts.get(opt.mHostKey));
    }


    private static EntityResult execute(String url, TubeOptions opt,
                                        LinkedList<String> hosts) throws TubeException {

        int method = opt.mHttpMethod;
        int soTimeOut = opt.mSoTimeOut;
        int connTimeOut = opt.mConnTimeOut;
        int reConnTimes = opt.mReconnTimes;
        List<Header> headers = opt.mListHeaders;
        Map<String, String> keyValues = opt.mMapHeaders;
        HttpEntity postEntity = opt.mPostEntity;
        String hostKey = opt.mHostKey;
        HttpHost proxy = opt.mHttpProxy;
        HttpRequestBase req = null;

        HttpClient client = null;

        if (url.startsWith("https")) {
            client = createHttpsClient();
        } else {
            client = new DefaultHttpClient();
        }

        if (proxy != null) {
            client.getParams().setParameter(ConnRouteParams.DEFAULT_PROXY,
                    proxy);
        }

        // 根据需要修改请求方式
        if (method == TubeOptions.HTTP_METHOD_GET) {
            req = new HttpGet(processUrls(url, hostKey, hosts));
        } else {
            HttpPost post = new HttpPost(processUrls(url, hostKey, hosts));
            if (postEntity != null) {
                post.setEntity(postEntity);
                reConnTimes = 1;// postentity太耗费流量，如果一次post失败建议不再重试
                // 20131126
            }
            req = post;
            post = null;
        }

        initParams(client, req, soTimeOut, connTimeOut, headers, keyValues);

        try {
            HttpResponse response = getEntity(client, req, hostKey, hosts,
                    reConnTimes, 0);
            HttpEntity entity = response.getEntity();
            return new EntityResult(client, req, entity, response);
        } catch (TubeException e) {
            if (!req.isAborted()) {
                req.abort();
            }

            client.getConnectionManager().shutdown();
            throw e;
        } catch (Exception e) {
            if (!req.isAborted()) {
                req.abort();
            }

            client.getConnectionManager().shutdown();
            throw new TubeException(e.getMessage(), TubeException.IO_ERROR_CODE);
        } finally {

        }
    }

    /**
     * 兼容Chunked模式
     *
     * @param entity
     * @return
     * @throws IllegalStateException
     * @throws IOException
     */
    private static String entity2String(HttpEntity entity)
            throws IllegalStateException, IOException {
        if (entity == null) {
            throw new IOException("Entity is null!");
        }
        Header contentEncoding = entity.getContentEncoding();
        boolean acceptGzip = contentEncoding != null
                && contentEncoding.getValue().equals("gzip");

        if (!entity.isChunked() && !acceptGzip) {
            return EntityUtils.toString(entity);
        }

        ByteArrayBuilder dataBuilder = new ByteArrayBuilder();

        InputStream is = entity.getContent();

        InputStream nis = null;
        byte[] buf = null;
        int count = 0;

        nis = acceptGzip ? new GZIPInputStream(is) : is;

        /*
         * accumulate enough data to make it worth pushing it up the stack
         */
        buf = new byte[8 * 1024];
        int len = 0;
        int lowWater = buf.length / 2;

        while (len != -1) {

            len = nis.read(buf, count, buf.length - count);

            if (len != -1) {
                count += len;
            }
            if (len == -1 || count >= lowWater) {
                dataBuilder.append(buf, 0, count);
                // Log.i("wei.han", "The length is " + count + " this time!");
                count = 0;
            }
        }

        if (is != null) {
            is.close();
        }

        if (nis != null) {
            nis.close();
        }

        synchronized (dataBuilder) {
            byte[] data = new byte[dataBuilder.getByteSize()];
            int offset = 0;
            while (true) {
                ByteArrayBuilder.Chunk c = dataBuilder.getFirstChunk();
                if (c == null)
                    break;

                if (c.mLength != 0) {
                    System.arraycopy(c.mArray, 0, data, offset, c.mLength);
                    offset += c.mLength;
                }
                c.release();
            }

            return new String(data);
        }
    }

    /**
     * 根据需要修改请求地址的主机和端口号
     *
     * @param url
     * @param hosts
     * @return
     */
    private static URI processUrls(String url, String hostKey,
                                   LinkedList<String> hosts) {

        URI uri = URI.create(url);

        // 如果没有主机切换地址或者从来没有切换过，则直接返回
        if (hosts == null || hosts.isEmpty() || !sIndexMap.containsKey(hostKey)) {
            return uri;
        }

        String authority = uri.getAuthority();

        Integer index = sIndexMap.get(hostKey);

        // 检查index是否越界 wei.han
        if (index == null || hosts.size() <= index) {
            return uri;
        }

        URI newServerURI = URI.create(hosts.get(index));

        uri = URI.create(url.replaceFirst(authority,
                newServerURI.getAuthority()));

        return uri;
    }

    /**
     * 初始化请求参数
     *
     * @param client
     * @param req
     * @param soTimeOut
     * @param connTimeOut
     * @param headers
     * @param keyValues
     */
    private static void initParams(HttpClient client, HttpRequestBase req,
                                   int soTimeOut, int connTimeOut, List<Header> headers,
                                   Map<String, String> keyValues) {

        if (req instanceof HttpPost) {
            ((DefaultHttpClient) client)
                    .setRedirectHandler(new DefaultRedirectHandler() {

                        @Override
                        public boolean isRedirectRequested(
                                HttpResponse response, HttpContext context) {
                            // TODO Auto-generated method stub
                            return false;
                        }

                    });
        }

        HttpParams params = client.getParams();

        if (soTimeOut > 0) {
            params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, soTimeOut);
        }

        if (connTimeOut > 0) {
            params.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,
                    connTimeOut);
        }

        // 请求可接受gzip流
        req.setHeader("Accept-Encoding", "gzip");

        // 设置Header列表
        if (headers != null) {
            for (Header header : headers) {
                req.setHeader(header);
            }
        }

        // 设置key-values 形式的 header
        if (keyValues != null) {
            for (String key : keyValues.keySet()) {
                req.setHeader(key, keyValues.get(key));
            }
        }

    }

    /**
     * 请求连接逻辑 处理连接失败等情况
     *
     * @param client
     * @param req
     * @param hosts
     * @param reConnTimes
     * @return
     * @throws TubeException
     */
    private static HttpResponse getEntity(HttpClient client, HttpRequestBase req,
                                          String hostKey, LinkedList<String> hosts, int reConnTimes, int times)
            throws TubeException, IllegalArgumentException {
        String headerStr = "";
        if (Logger.IS_DEBUG_MODE) {
            Header[] headers = req.getAllHeaders();
            StringBuilder sb = new StringBuilder();
            for (Header header : headers) {
                sb.append(header.getName() + ":" + header.getValue() + ";");
            }
            headerStr = sb.toString();
        }

        // 请求服务器
        HttpResponse response = null;

        long totalResponseTime = 0;
        long startTime = System.currentTimeMillis();

        for (int i = 0; i <= reConnTimes; i++) {

            // 如果不是第一次连接，并且hosts地址不为null，则需要切换服务器地址
            if (hosts != null && !hosts.isEmpty() && i > 0) {
                switchHost(req, hostKey, hosts);
            }

            try {
                response = client.execute(req);
                totalResponseTime += (System.currentTimeMillis() - startTime);
            } catch (IOException e) {
                totalResponseTime += (System.currentTimeMillis() - startTime);
                if (i != reConnTimes) {
                    waitToReconnect();
                }

                continue;
            } catch (Exception e) {
                throw new TubeException("The exception is unknow: "
                        + e.getMessage(), TubeException.IO_ERROR_CODE);
            }

            // 获取http状态码
            int status = response.getStatusLine().getStatusCode();
            if (status == HttpStatus.SC_OK) {
                return response;
            }
            Logger.e("TUBE",""+"HttpStatus is not OK. -> " + status);
           return response;
//            //由于验证验证码接口，就算抛出异常，也要根据body中的属性判断是否通过
//            HttpEntity entity = response.getEntity();
//            String result = null;
//            try {
//                result = HttpConnector.entity2String(entity);
//                JSONObject jsonObject = new JSONObject(result);
//                String errorType = jsonObject.optString("errorType");
//                if ("InvalidName".equals(errorType)) {
//                    return response;
//                }
//                if ("InvalidPassword".equals(errorType)) {
//                    return response;
//                }
//                if ("InvalidPhoneNumber".equals(errorType)) {
//                    return response;
//                }
//                if ("PhoneNumberAlreadyRegistered".equals(errorType)) {
//                    return response;
//                }
//                if ("InvalidPhoneVCode".equals(errorType)) {
//                    return response;
//                }
//                if ("InvalidLanguage".equals(errorType)) {
//                    return response;
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            // http状态不正确,主动抛出异常
//            throw new TubeException("HttpStatus is not OK. -> " + status ,
//                    TubeException.SERVER_ERROR_CODE);
        }

        // 如果进入到这个位置，说明所有重连已经结束，仍然无法获取数据，则抛出重连失败异常
        throw new TubeException(
                "All connections is failed. Please check the network.",
                TubeException.IO_ERROR_CODE);
    }

    private static void waitToReconnect() {
        try {
            Thread.sleep(RECONN_INTERVAL);
        } catch (InterruptedException e) {

        }
    }

    /**
     * 切换主机
     *
     * @param req
     * @param hosts
     */
    private static void switchHost(HttpRequestBase req, String hostKey,
                                   LinkedList<String> hosts) {
        if (!sIndexMap.containsKey(hostKey)) {
            sIndexMap.put(hostKey, 0);
        }

        int index = sIndexMap.get(hostKey) + 1;

        if (index >= hosts.size()) {
            index = 0;
        }

        sIndexMap.put(hostKey, index);

        URI newHostUri = URI.create(hosts.get(index));
        String newHost = newHostUri.getHost();

        URI uri = req.getURI();
        String oldHost = uri.getHost();
        String newUrl = uri.toString().replaceFirst(oldHost, newHost);
        req.setURI(URI.create(newUrl));

    }

    /**
     * 创建一个自定义的额HttpCLient,包括对Https的证书忽略
     *
     * @return
     */
    private static HttpClient createHttpsClient() {

        SSLSocketFactoryEx sf = null;

        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore
                    .getDefaultType());
            trustStore.load(null, null);
            sf = new SSLSocketFactoryEx(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Logger.d(TAG, e.getLocalizedMessage());
        }

        if (sf == null) {
            return new DefaultHttpClient();
        }

        HttpParams params = new BasicHttpParams();
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(params,
                HTTP.DEFAULT_CONTENT_CHARSET);
        HttpProtocolParams.setUseExpectContinue(params, true);
        SchemeRegistry schReg = new SchemeRegistry();
        schReg.register(new Scheme("http", PlainSocketFactory
                .getSocketFactory(), 80));
        schReg.register(new Scheme("https", sf, 443));
        ClientConnectionManager conMgr = new ThreadSafeClientConnManager(
                params, schReg);
        return new DefaultHttpClient(conMgr, params);
    }

    /**
     * 对返回的entity进行封装
     *
     * @author zhujunyu
     */
    public static class EntityResult {

        private HttpClient mClient = null;
        private HttpEntity mEntity = null;
        private HttpRequestBase mRequest = null;

        private HttpResponse mResponse = null;

        /**
         * 唯一构造方法，使用client request 和 entity构造
         */
        public EntityResult(HttpClient client, HttpRequestBase req,
                            HttpEntity entity, HttpResponse response) {
            mClient = client;
            mRequest = req;
            mEntity = entity;
            mResponse = response;
        }

        /**
         * 自动读取entity中的流为String ，读取后无法再次读取
         *
         * @return
         */
        public String entity2String() {
            if (mEntity != null) {
                try {

                    String result = HttpConnector.entity2String(mEntity);
                    Logger.d(TAG, "Result: \n" + result);
                    return result;
                } catch (IllegalStateException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } finally {

                }
            }

            return null;
        }

        /**
         * 获得entity中的流对象
         *
         * @return
         */
        public InputStream entity2Stream() {
            if (mEntity != null) {
                try {
                    return mEntity.getContent();
                } catch (IllegalStateException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            return null;
        }

        public HttpResponse getHttpResponce() {
            return mResponse;
        }

        /**
         * 关闭此次连接并释放资源
         */
        public void close() {
            if (mRequest != null && !mRequest.isAborted()) {
                mRequest.abort();
            }

            if (mClient != null) {
                mClient.getConnectionManager().shutdown();
            }
        }
    }
}
