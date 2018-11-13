package com.movit.platform.framework.utils;

import android.text.TextUtils;
import com.alibaba.fastjson.JSONObject;
import com.movit.platform.common.okhttp.utils.AesUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

/**
 * Created by Administrator on 2014/6/25.
 */
public class HttpClientUtils {

    private static HttpClient httpClient;
    static {
        httpClient = new DefaultHttpClient();
        // 请求超时
        httpClient.getParams().setParameter(
                HttpConnectionParams.CONNECTION_TIMEOUT, 10000);
        // 读取超时
        httpClient.getParams().setParameter(HttpConnectionParams.SO_TIMEOUT,
                10000);
    }

    public static String doPost(String url, Map<String, String> params,
                                String encoding) throws IOException {
        UrlEncodedFormEntity formEntity = buildUrlEncodedFormEntity(encoding,
                params);

        LogUtils.v("HttpRequest", "url：" + url);
        LogUtils.v("HttpRequest", "content：" + Obj2Json.map2json(params));

        HttpPost httpPost = buildHttpPost(url, formEntity);
        HttpResponse response = httpClient.execute(httpPost);

        String str = EntityUtils.toString(response.getEntity(), encoding);
        LogUtils.v("HttpRequest", "response：" + str);

        return str;
    }

    private static HttpPost buildHttpPost(String url, HttpEntity entity) {
        HttpPost httpPost = new HttpPost(url);
        if (entity != null) {
            httpPost.setEntity(entity);
        }
        return httpPost;
    }

    private static UrlEncodedFormEntity buildUrlEncodedFormEntity(
            String encoding, Map<String, String> nameValuePairs)
            throws UnsupportedEncodingException {
        List<NameValuePair> formParams = new ArrayList<NameValuePair>();
        if (nameValuePairs != null) {
            for (Map.Entry<String, String> nameValueEntry : nameValuePairs
                    .entrySet()) {
                formParams.add(new BasicNameValuePair(nameValueEntry.getKey(),
                        nameValueEntry.getValue()));
            }
        }
        return new UrlEncodedFormEntity(formParams, encoding);
    }

    public static String post(String url, String content, Charset charset) {

        LogUtils.v("HttpRequest", "url：" + url);
        LogUtils.v("HttpRequest", "content：" + content);

        String result = null;
        DefaultHttpClient httpClient = new DefaultHttpClient();
        // 请求超时
        httpClient.getParams().setParameter(
                HttpConnectionParams.CONNECTION_TIMEOUT, 10000);
        // 读取超时
        httpClient.getParams().setParameter(HttpConnectionParams.SO_TIMEOUT,
                10000);
        try {
            HttpPost httpPost = new HttpPost(url);

            if (content != null && content.length() > 0) {
                httpPost.addHeader("Content-Type",
                        "application/json;charset=UTF-8");
                StringEntity se = new StringEntity("{\"secretMsg\":\"" + AesUtils.getInstance().encrypt(content) + "\"}", HTTP.UTF_8);
                httpPost.setEntity(se);
            }

            HttpResponse httpResponse = httpClient.execute(httpPost);

            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent(), charset));
            for (String s = reader.readLine(); s != null; s = reader.readLine()) {
                builder.append(s);
            }
            result = builder.toString();

            LogUtils.v("HttpRequest", "response：" + result);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.getConnectionManager().shutdown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (!TextUtils.isEmpty(result)){
            JSONObject resultStr = JSONObject.parseObject(result);
            return AesUtils.getInstance().decrypt(resultStr.get("resultStr").toString());
        }else {
            return "{}";
        }
    }

    public static String postWithoutEncode(String url, String content, Charset charset) {

        LogUtils.v("HttpRequest", "url：" + url);
        LogUtils.v("HttpRequest", "content：" + content);

        String result = null;
        DefaultHttpClient httpClient = new DefaultHttpClient();
        // 请求超时
        httpClient.getParams().setParameter(
                HttpConnectionParams.CONNECTION_TIMEOUT, 10000);
        // 读取超时
        httpClient.getParams().setParameter(HttpConnectionParams.SO_TIMEOUT,
                10000);
        try {
            HttpPost httpPost = new HttpPost(url);

            if (content != null && content.length() > 0) {
                httpPost.addHeader("Content-Type",
                        "application/json;charset=UTF-8");
                StringEntity se = new StringEntity("{\"secretMsg\":\"" + AesUtils.getInstance().encrypt(content) + "\"}", HTTP.UTF_8);
                httpPost.setEntity(se);
            }

            HttpResponse httpResponse = httpClient.execute(httpPost);

            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent(), charset));
            for (String s = reader.readLine(); s != null; s = reader.readLine()) {
                builder.append(s);
            }
            result = builder.toString();

            LogUtils.v("HttpRequest", "response：" + result);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.getConnectionManager().shutdown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (!TextUtils.isEmpty(result)){
            JSONObject resultStr = JSONObject.parseObject(result);
            return AesUtils.getInstance().decrypt(resultStr.get("resultStr").toString());
        }else {
            return "{}";
        }
    }

    public static String postWithoutEncrypt(String url) {
        LogUtils.v("HttpRequest", "url：" + url);
        BufferedReader in = null;
        String responseStr = "";
        try {
            HttpClient client = new DefaultHttpClient();
            // 请求超时
            client.getParams().setParameter(
                    HttpConnectionParams.CONNECTION_TIMEOUT, 10000);
            // 读取超时
            client.getParams().setParameter(HttpConnectionParams.SO_TIMEOUT,
                    10000);
            HttpPost request = new HttpPost(url);
            HttpResponse response = client.execute(request);
            in = new BufferedReader(new InputStreamReader(response.getEntity()
                    .getContent()));
            StringBuffer sb = new StringBuffer("");
            String line = "";
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            in.close();
            responseStr = sb.toString();
            LogUtils.v("HttpRequest", "responseStr：" + responseStr);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
        return responseStr;
    }

    public static String get(String url) {
        LogUtils.v("HttpRequest", "url：" + url);
        BufferedReader in = null;
        String responseStr = "";
        try {
            HttpClient client = new DefaultHttpClient();
            // 请求超时
            client.getParams().setParameter(
                    HttpConnectionParams.CONNECTION_TIMEOUT, 10000);
            // 读取超时
            client.getParams().setParameter(HttpConnectionParams.SO_TIMEOUT,
                    10000);
            HttpGet request = new HttpGet(url);
            HttpResponse response = client.execute(request);
            in = new BufferedReader(new InputStreamReader(response.getEntity()
                    .getContent()));
            StringBuffer sb = new StringBuffer("");
            String line = "";
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            in.close();
            responseStr = sb.toString();
            LogUtils.v("HttpRequest", "responseStr：" + responseStr);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
        return responseStr;
    }

    public static String post(String url, Map<String, String> params) {
        LogUtils.v("HttpRequest", "url：" + url);
        BufferedReader in = null;
        String result = "";
        try {
            HttpClient client = new DefaultHttpClient();
            // 请求超时
            client.getParams().setParameter(
                    HttpConnectionParams.CONNECTION_TIMEOUT, 10000);
            // 读取超时
            client.getParams().setParameter(HttpConnectionParams.SO_TIMEOUT,
                    10000);
            HttpPost request = new HttpPost(url);
            List<NameValuePair> postParams = new ArrayList<NameValuePair>();
            if (params != null) {
                for (Map.Entry<String, String> nameValueEntry : params
                        .entrySet()) {
                    postParams.add(new BasicNameValuePair(nameValueEntry
                            .getKey(), nameValueEntry.getValue()));
                }
            }
            UrlEncodedFormEntity formEntity = new UrlEncodedFormEntity(
                    postParams, "UTF-8");
            request.setEntity(formEntity);
            HttpResponse response = client.execute(request);
            in = new BufferedReader(new InputStreamReader(response.getEntity()
                    .getContent()));
            StringBuffer sb = new StringBuffer("");
            String line = "";
            while ((line = in.readLine()) != null) {
                sb.append(line);
            }
            in.close();
            result = sb.toString();
            LogUtils.v("HttpRequest", "response：" + result);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
        return result;
    }

    public static String postZone(String url, String content, Charset charset) {
        String result = null;
        DefaultHttpClient httpClient = new DefaultHttpClient();
        // 请求超时
        httpClient.getParams().setParameter(
                HttpConnectionParams.CONNECTION_TIMEOUT, 10000);
        // 读取超时
        httpClient.getParams().setParameter(HttpConnectionParams.SO_TIMEOUT,
                10000);
        try {
            HttpPost httpPost = new HttpPost(url);

            if (content != null && content.length() > 0) {
                httpPost.addHeader("Content-Type",
                        "application/x-www-form-urlencoded;charset=UTF-8");
                StringEntity se = new StringEntity(content, HTTP.UTF_8);
                httpPost.setEntity(se);
            }
            HttpResponse httpResponse = httpClient.execute(httpPost);

            StringBuilder builder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    httpResponse.getEntity().getContent(), charset));
            for (String s = reader.readLine(); s != null; s = reader.readLine()) {
                builder.append(s);
            }
            result = builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (httpClient != null) {
                try {
                    httpClient.getConnectionManager().shutdown();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        LogUtils.v("HttpRequest", "response：" + result);

        return result;
    }

//    public static boolean isNetAvailable(Activity activity) {
//        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
//        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
//        if (null != networkInfo) {
//            return networkInfo.isAvailable();
//        }
//        return false;
//    }
//
//    public static void downFile(String fileLink, String SDpath, String username, String password, UpdataBarListerner ubl) {
//        String fileName = FileUtils.getInstance().geFileName(fileLink);
//
//        fileName = URLDecoder.decode(URLDecoder.decode(fileName));
//
//        // 在这个地方判断文件是否已经存在? 文件是否已经在服务器端更新过了? File.lastModified() 好象不行，一会再看看。
//        // 首先先简单处理一下：直接先删除。
//        if (FileUtils.getInstance().isFileExist(SDpath + fileName)) {
//            File file = new File(SDpath + fileName);
//            file.delete();
//        }
//        InputStream inputStream = null;
//        try {
//            inputStream = getInputStreamFromUrl(fileLink, username, password);
//            FileUtils.getInstance().write2SDFromInput(SDpath, fileName, inputStream, ubl);
//        } catch (Exception e) {
//            if (FileUtils.getInstance().isFileExist(SDpath + fileName)) {
//                File file = new File(SDpath + fileName);
//                file.delete();
//            }
//            ubl.onError(HomeFragment.ERROR_DOWNLOAD_ATTACHMENT, 0);
//            e.printStackTrace();
//        } finally {
//            try {
//                if (null != inputStream) {
//                    inputStream.close();
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    public static InputStream getInputStreamFromUrl(String webserviceUrl, String username, String password) {
//        InputStream inputStream = null;
//        try {
//            String deviceIP = null; // FileUtils.getInstance().getLocalIpAddress();
//            // Device IP
//            String domainName = FileUtils.DOMAIN_NAME; // Domain name
//
//            DefaultHttpClient httpclient = new DefaultHttpClient();
//            httpclient.getAuthSchemes().register("ntlm", new NTLMSchemeFactory());
//            AuthScope authScope = new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT);
//            AuthScope authScope1 = new AuthScope(null, -1);
//            httpclient.getCredentialsProvider().setCredentials(authScope,
//                    new NTCredentials(username, password, deviceIP, domainName));
//
//            HttpGet httpGet = new HttpGet(webserviceUrl);
//            httpGet.getParams().setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
//
//            HttpResponse response = httpclient.execute(httpGet);
//            // String responseXML = EntityUtils.toString(response.getEntity());
//
//            if (HttpStatus.SC_OK == response.getStatusLine().getStatusCode()) {
//                HttpEntity entity = response.getEntity();
//                inputStream = entity.getContent();
//                return inputStream;
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return inputStream;
//    }


}
