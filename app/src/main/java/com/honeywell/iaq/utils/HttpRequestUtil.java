package com.honeywell.iaq.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.content.Context;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import com.honeywell.iaq.bean.FormFile;

/* 
 *
 * */
public class HttpRequestUtil {
//    /**
//     * @param url
//     * @param params
//     * @param headers
//     * @return
//     * @throws Exception
//     */
//    public static URLConnection sendGetRequest(String url, Map<String, String> params, Map<String, String> headers) throws Exception {
//        StringBuilder buf = new StringBuilder(url);
//        Set<Entry<String, String>> entrys = null;
//
//        if (params != null && !params.isEmpty()) {
//            buf.append("?");
//            entrys = params.entrySet();
//            for (Entry<String, String> entry : entrys) {
//                buf.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8")).append("&");
//            }
//            buf.deleteCharAt(buf.length() - 1);
//        }
//        URL url1 = new URL(buf.toString());
//        HttpURLConnection conn = (HttpURLConnection) url1.openConnection();
//        conn.setRequestMethod("GET");
//
//        if (headers != null && !headers.isEmpty()) {
//            entrys = headers.entrySet();
//            for (Entry<String, String> entry : entrys) {
//                conn.setRequestProperty(entry.getKey(), entry.getValue());
//            }
//        }
//        conn.getResponseCode();
//        return conn;
//    }

//    /**
//     * @param url
//     * @param params
//     * @param headers
//     * @return
//     * @throws Exception
//     */
//    public static URLConnection sendPostRequest(String url, Map<String, String> params, Map<String, String> headers) throws Exception {
//        StringBuilder buf = new StringBuilder();
//        Set<Entry<String, String>> entrys = null;
//
//        if (params != null && !params.isEmpty()) {
//            entrys = params.entrySet();
//            for (Entry<String, String> entry : entrys) {
//                buf.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), "UTF-8")).append("&");
//            }
//            buf.deleteCharAt(buf.length() - 1);
//        }
//        URL url1 = new URL(url);
//        Log.d("sendPostRequest", "URL=" + url1);
//        HttpURLConnection conn = (HttpURLConnection) url1.openConnection();
//        conn.setRequestMethod("POST");
//        conn.setDoOutput(true);
//        OutputStream out = conn.getOutputStream();
//        out.write(buf.toString().getBytes("UTF-8"));
//        if (headers != null && !headers.isEmpty()) {
//            entrys = headers.entrySet();
//            for (Entry<String, String> entry : entrys) {
//                conn.setRequestProperty(entry.getKey(), entry.getValue());
//            }
//        }
//        int responseCode = conn.getResponseCode();
//        Log.d("sendPostRequest", "responseCode=" + responseCode);
//        return conn;
//    }
//
//    /*
//     * Function :
//     */
//    public static String submitPostData(Context context, String urlStr, String jsonStr, boolean isReadResponse) {
//        byte[] data = jsonStr.getBytes();
//        try {
//            URL url = new URL(urlStr);
//            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
//            httpURLConnection.setConnectTimeout(3000);
//            httpURLConnection.setDoInput(true);
//            httpURLConnection.setDoOutput(true);
//            httpURLConnection.setRequestMethod("POST");
//            httpURLConnection.setUseCaches(false);
//            httpURLConnection.setRequestProperty("Content-Type", "application/json");
//            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
//            String cookie = getCookie(context);
//            Log.d("submitPostData", "GetCookie: " + cookie);
//            if (cookie != null) {
//                httpURLConnection.setRequestProperty(Constants.KEY_COOKIE, cookie);
//                Utils.setSharedPreferencesValue(context, Constants.KEY_COOKIE, cookie);
//            }
//
//            OutputStream outputStream = httpURLConnection.getOutputStream();
//            outputStream.write(data);
//
//            int response = httpURLConnection.getResponseCode();
//            Log.d("submitPostData", "responseCode=" + response);
//            if (response == HttpURLConnection.HTTP_OK) {
//                String setCookie = httpURLConnection.getHeaderField(Constants.KEY_SET_COOKIE);
//                if (setCookie != null) {
//                    setCookie(context, setCookie);
//                }
//
//                if (isReadResponse) {
//                    InputStream inputStream = httpURLConnection.getInputStream();
//                    String responseStr = dealResponseResult(inputStream);
//                    if (responseStr.contains(Constants.KEY_PHONE_ID)) {
//                        responseStr = Utils.replaceBlank(responseStr);
//                        int phoneIdIndex = responseStr.indexOf(Constants.KEY_PHONE_ID) + Constants.KEY_PHONE_ID.length();
//                        int start = responseStr.indexOf('\"', phoneIdIndex + 1) + 1;
//                        int end = responseStr.lastIndexOf('\"');
//                        String phoneId = responseStr.substring(start, end);
//                        Utils.setSharedPreferencesValue(context, Constants.KEY_PHONE_ID, phoneId);
//                        Log.d("submitPostData", "PhoneId=" + phoneId);
//                    } else if (responseStr.contains(Constants.KEY_DEVICE_ID)) {
//                        responseStr = Utils.replaceBlank(responseStr);
//                        int phoneIdIndex = responseStr.indexOf(Constants.KEY_DEVICE_ID) + Constants.KEY_DEVICE_ID.length();
//                        int start = responseStr.indexOf('\"', phoneIdIndex + 1) + 1;
//                        int end = responseStr.lastIndexOf('\"');
//                        String deviceId = responseStr.substring(start, end);
//                        Utils.setSharedPreferencesValue(context, Constants.KEY_DEVICE_ID, deviceId);
//                        Log.d("submitPostData", "DeviceId=" + deviceId);
//                    } else {
//                        return responseStr;
//                    }
//                }
//                Log.d("submitPostData", "Cookie: " + setCookie);
//                return "Post success";
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//
//        }
//        return "";
//    }

//    public static String submitPostData(Context context, String urlStr, String jsonStr) {
//        byte[] data = jsonStr.getBytes();
//        OutputStream outputStream = null;
//        InputStream inputStream = null;
//        try {
//            URL url = new URL(urlStr);
//            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
//            httpURLConnection.setConnectTimeout(3000);
//            httpURLConnection.setDoInput(true);
//            httpURLConnection.setDoOutput(true);
//            httpURLConnection.setRequestMethod("POST");
//            httpURLConnection.setUseCaches(false);
//            httpURLConnection.setRequestProperty("Content-Type", "application/json");
//            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
//            String cookie = getCookie(context);
//            Log.d("submitPostData", "GetCookie: " + cookie);
//            if (cookie != null) {
//                httpURLConnection.setRequestProperty(Constants.KEY_COOKIE, cookie);
//                Utils.setSharedPreferencesValue(context, Constants.KEY_COOKIE, cookie);
//            }
//
//            outputStream = httpURLConnection.getOutputStream();
//            outputStream.write(data);
//
//            int response = httpURLConnection.getResponseCode();
//            Log.d("submitPostData", "responseCode=" + response);
//            if (response == HttpURLConnection.HTTP_OK) {
//                String setCookie = httpURLConnection.getHeaderField(Constants.KEY_SET_COOKIE);
//                if (setCookie != null) {
//                    setCookie(context, setCookie);
//                }
//
//                inputStream = httpURLConnection.getInputStream();
//                String responseStr = dealResponseResult(inputStream);
//                Log.d("submitPostData", "responseStr=" + responseStr);
//                return responseStr;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (outputStream != null) {
//                    outputStream.close();
//                }
//                if (inputStream != null) {
//                    inputStream.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return "";
//    }
//
//    private String getRequestHeader(HttpURLConnection conn) {
//        Map<String, List<String>> requestHeaderMap = conn.getRequestProperties();
//        Iterator<String> requestHeaderIterator = requestHeaderMap.keySet().iterator();
//        StringBuilder sbRequestHeader = new StringBuilder();
//        while (requestHeaderIterator.hasNext()) {
//            String requestHeaderKey = requestHeaderIterator.next();
//            String requestHeaderValue = conn.getRequestProperty(requestHeaderKey);
//            sbRequestHeader.append(requestHeaderKey);
//            sbRequestHeader.append(":");
//            sbRequestHeader.append(requestHeaderValue);
//            sbRequestHeader.append("\n");
//        }
//        return sbRequestHeader.toString();
//    }
//
//    private static String getResponseHeader(HttpURLConnection conn) {
//        Map<String, List<String>> responseHeaderMap = conn.getHeaderFields();
//        int size = responseHeaderMap.size();
//        StringBuilder sbResponseHeader = new StringBuilder();
//        for (int i = 0; i < size; i++) {
//            String responseHeaderKey = conn.getHeaderFieldKey(i);
//            String responseHeaderValue = conn.getHeaderField(i);
//            sbResponseHeader.append(responseHeaderKey);
//            sbResponseHeader.append(":");
//            sbResponseHeader.append(responseHeaderValue);
//            sbResponseHeader.append("\n");
//        }
//        return sbResponseHeader.toString();
//    }
//
    public static String getCookie(Context context) {
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        String cookie = cookieManager.getCookie(Constants.KEY_COOKIE);
        return cookie;
    }

    public static void setCookie(Context context, String cookie) {
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.removeSessionCookie();
        cookieManager.setCookie(Constants.KEY_COOKIE, cookie);
        CookieSyncManager.getInstance().sync();
    }
//
//    /*
//     * Function :
//     */
//    public static String submitGetData(Context context, String urlStr, String jsonStr, String encode, boolean isReadResponse) {
//        byte[] data = jsonStr.getBytes();
//        try {
//            URL url = new URL(urlStr);
//            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
//            httpURLConnection.setConnectTimeout(3000);
//            httpURLConnection.setDoInput(true);
//            httpURLConnection.setDoOutput(true);
//            httpURLConnection.setRequestMethod("GET");
//            httpURLConnection.setUseCaches(false);
//            httpURLConnection.setRequestProperty("Content-Type", "application/json");
//            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(data.length));
//            String cookie = getCookie(context);
//            Log.d("submitGetData", "GetCookie: " + cookie);
//            if (cookie != null) {
//                httpURLConnection.setRequestProperty(Constants.KEY_COOKIE, cookie);
//            }
//
//            OutputStream outputStream = httpURLConnection.getOutputStream();
//            outputStream.write(data);
//
//            int response = httpURLConnection.getResponseCode();
//            Log.d("submitGetData", "responseCode=" + response);
//            if (response == HttpURLConnection.HTTP_OK) {
//                String setCookie = httpURLConnection.getHeaderField(Constants.KEY_SET_COOKIE);
//                Log.d("submitGetData", "setCookie: " + setCookie);
//                if (setCookie != null) {
//                    setCookie(context, setCookie);
//                }
//                return "Get success";
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return "";
//    }

//    /*
//     * Function :
//     */
//    public static String getGetData(Context context, String urlStr) {
//        InputStream inputStream = null;
//        try {
//            URL url = new URL(urlStr);
//            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
//            httpURLConnection.setRequestMethod("GET");
//            httpURLConnection.setConnectTimeout(3000);
//            httpURLConnection.setDoInput(true);
//            String cookie = HttpRequestUtil.getCookie(context);
//            Log.d("getBoundDevices", "GetCookie: " + cookie);
//            if (cookie != null) {
//                httpURLConnection.setRequestProperty(Constants.KEY_COOKIE, cookie);
//            }
//
//            int response = httpURLConnection.getResponseCode();
//            Log.d("getBoundDevices", "responseCode=" + response);
//            if (response == HttpURLConnection.HTTP_OK) {
//                inputStream = httpURLConnection.getInputStream();
//                String responseStr = HttpRequestUtil.dealResponseResult(inputStream);
//                Log.d("getBoundDevices", "Response string=" + responseStr);
//                return responseStr;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            try {
//                if (inputStream != null) {
//                    inputStream.close();
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//        return "";
//    }

    /*
     * Function :
     */
    public static String dealResponseResult(InputStream inputStream) {
        String resultData = null;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int len = 0;
        try {
            while ((len = inputStream.read(data)) != -1) {
                byteArrayOutputStream.write(data, 0, len);
            }
            resultData = new String(byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return resultData;
    }

//    /*
//     * Function :
//     */
//    public static StringBuffer getRequestData(Map<String, String> params, String encode) {
//        StringBuffer stringBuffer = new StringBuffer();
//        try {
//            for (Entry<String, String> entry : params.entrySet()) {
//                stringBuffer.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), encode)).append("&");
//            }
//            stringBuffer.deleteCharAt(stringBuffer.length() - 1);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return stringBuffer;
//    }

//    /**
//     * <FORM METHOD=POST
//     * ACTION="http://192.168.0.200:8080/ssi/fileload/test.do"
//     * enctype="multipart/form-data"> <INPUT TYPE="text" NAME="name"> <INPUT
//     * TYPE="text" NAME="id"> <input type="file" name="imagefile"/> <input
//     * type="file" name="zip"/> </FORM>
//     *
//     * @param path
//     * @param params
//     * @param files
//     */
//    public static boolean uploadFiles(String path, Map<String, String> params, FormFile[] files) throws Exception {
//        final String BOUNDARY = "---------------------------7da2137580612";
//        final String endline = "--" + BOUNDARY + "--\r\n";
//
//        int fileDataLength = 0;
//        if (files != null && files.length != 0) {
//            for (FormFile uploadFile : files) {
//                StringBuilder fileExplain = new StringBuilder();
//                fileExplain.append("--");
//                fileExplain.append(BOUNDARY);
//                fileExplain.append("\r\n");
//                fileExplain.append("Content-Disposition: form-data;name=\"" + uploadFile.getParameterName() + "\";filename=\"" + uploadFile.getFilname() + "\"\r\n");
//                fileExplain.append("Content-Type: " + uploadFile.getContentType() + "\r\n\r\n");
//                fileExplain.append("\r\n");
//                fileDataLength += fileExplain.length();
//                if (uploadFile.getInStream() != null) {
//                    fileDataLength += uploadFile.getFile().length();
//                } else {
//                    fileDataLength += uploadFile.getData().length;
//                }
//            }
//        }
//        StringBuilder textEntity = new StringBuilder();
//        if (params != null && !params.isEmpty()) {
//            for (Entry<String, String> entry : params.entrySet()) {
//                textEntity.append("--");
//                textEntity.append(BOUNDARY);
//                textEntity.append("\r\n");
//                textEntity.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n");
//                textEntity.append(entry.getValue());
//                textEntity.append("\r\n");
//            }
//        }
//
//        int dataLength = textEntity.toString().getBytes().length + fileDataLength + endline.getBytes().length;
//
//        URL url = new URL(path);
//        int port = url.getPort() == -1 ? 80 : url.getPort();
//        Socket socket = new Socket(InetAddress.getByName(url.getHost()), port);
//        OutputStream outStream = socket.getOutputStream();
//
//        String requestmethod = "POST " + url.getPath() + " HTTP/1.1\r\n";
//        outStream.write(requestmethod.getBytes());
//        String accept = "Accept: image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*\r\n";
//        outStream.write(accept.getBytes());
//        String language = "Accept-Language: zh-CN\r\n";
//        outStream.write(language.getBytes());
//        String contenttype = "Content-Type: multipart/form-data; boundary=" + BOUNDARY + "\r\n";
//        outStream.write(contenttype.getBytes());
//        String contentlength = "Content-Length: " + dataLength + "\r\n";
//        outStream.write(contentlength.getBytes());
//        String alive = "Connection: Keep-Alive\r\n";
//        outStream.write(alive.getBytes());
//        String host = "Host: " + url.getHost() + ":" + port + "\r\n";
//        outStream.write(host.getBytes());
//
//        outStream.write("\r\n".getBytes());
//
//        outStream.write(textEntity.toString().getBytes());
//
//        if (files != null && files.length != 0) {
//            for (FormFile uploadFile : files) {
//                StringBuilder fileEntity = new StringBuilder();
//                fileEntity.append("--");
//                fileEntity.append(BOUNDARY);
//                fileEntity.append("\r\n");
//                fileEntity.append("Content-Disposition: form-data;name=\"" + uploadFile.getParameterName() + "\";filename=\"" + uploadFile.getFilname() + "\"\r\n");
//                fileEntity.append("Content-Type: " + uploadFile.getContentType() + "\r\n\r\n");
//                outStream.write(fileEntity.toString().getBytes());
//                if (uploadFile.getInStream() != null) {
//                    byte[] buffer = new byte[1024];
//                    int len = 0;
//                    while ((len = uploadFile.getInStream().read(buffer, 0, 1024)) != -1) {
//                        outStream.write(buffer, 0, len);
//                    }
//                    uploadFile.getInStream().close();
//                } else {
//                    outStream.write(uploadFile.getData(), 0, uploadFile.getData().length);
//                }
//                outStream.write("\r\n".getBytes());
//            }
//        }
//
//        outStream.write(endline.getBytes());
//        BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//        if (reader.readLine().indexOf("200") == -1) {
//            return false;
//        }
//        outStream.flush();
//        outStream.close();
//        reader.close();
//        socket.close();
//        return true;
//    }


//    public static boolean uploadFile(String path, Map<String, String> params, FormFile file) throws Exception {
//        return uploadFiles(path, params, new FormFile[]{file});
//    }

//    /**
//     * @param inStream
//     * @return
//     * @throws Exception
//     */
//    public static byte[] read2Byte(InputStream inStream) throws Exception {
//        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
//        byte[] buffer = new byte[1024];
//        int len = 0;
//        while ((len = inStream.read(buffer)) != -1) {
//            outSteam.write(buffer, 0, len);
//        }
//        outSteam.close();
//        inStream.close();
//        return outSteam.toByteArray();
//    }

//    /**
//     * @param inStream
//     * @return
//     * @throws Exception
//     */
//    public static String read2String(InputStream inStream) throws Exception {
//        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
//        byte[] buffer = new byte[1024];
//        int len = 0;
//        while ((len = inStream.read(buffer)) != -1) {
//            outSteam.write(buffer, 0, len);
//        }
//        outSteam.close();
//        inStream.close();
//        return new String(outSteam.toByteArray(), "UTF-8");
//    }
//
//
//    public static byte[] postXml(String path, String xml, String encoding) throws Exception {
//        byte[] data = xml.getBytes(encoding);
//        URL url = new URL(path);
//        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//        conn.setRequestMethod("POST");
//        conn.setDoOutput(true);
//        conn.setRequestProperty("Content-Type", "text/xml; charset=" + encoding);
//        conn.setRequestProperty("Content-Length", String.valueOf(data.length));
//        conn.setConnectTimeout(5 * 1000);
//        OutputStream outStream = conn.getOutputStream();
//        outStream.write(data);
//        outStream.flush();
//        outStream.close();
//        if (conn.getResponseCode() == 200) {
//            return read2Byte(conn.getInputStream());
//        }
//        return null;
//    }
//
//
//    public static void main(String args[]) throws Exception {
//        Map<String, String> params = new HashMap<String, String>();
//        params.put("name", "xiazdong");
//        params.put("age", "10");
//        HttpURLConnection conn = (HttpURLConnection) HttpRequestUtil.sendGetRequest("http://192.168.0.103:8080/Server/PrintServlet", params, null);
//        int code = conn.getResponseCode();
//        InputStream in = conn.getInputStream();
//        byte[] data = read2Byte(in);
//    }
}
