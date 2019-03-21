package com.frame.server.imp;

import com.frame.config.ContentTypeEnum;
import com.frame.server.HttpService;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HttpServiceImp implements HttpService {
    private static final Logger logger = Logger.getLogger(HttpServiceImp.class);

    /**
     * 以body形式执行POST请求
     * @param url       请求url
     * @param header    请求头部信息
     * @param body      请求内容体
     * @return
     * @throws IOException
     */
    @Override
    public String postWithBody(String url, Map<String, String> header, String body) throws IOException {
        CloseableHttpResponse response = null;
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);
            setHeaderAndConfig(header,httpPost,ContentTypeEnum.BODY);
            httpPost.setEntity(new StringEntity(body, "UTF-8"));
            logger.info(String.format("请求URL: %s",url));
            response = httpClient.execute(httpPost);
            return getResponse(response);
        }finally {
            closeResponse(response);
        }
    }

    /**
     * 以form表单形式执行POST请求
     * @param url       请求url
     * @param header    请求头部信息
     * @param params    请求form表单内容
     * @return
     * @throws IOException
     */
    @Override
    public String postWithForm(String url, Map<String, String> header, Map<String,String> params) throws IOException {
        CloseableHttpResponse response = null;
        try {
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(url);
            setHeaderAndConfig(header,httpPost,ContentTypeEnum.FORM);
            //装填参数
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            if(params != null){
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    nvps.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
                }
            }
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
            logger.info(String.format("请求URL: %s",url));
            response = httpClient.execute(httpPost);
            return getResponse(response);
        }finally {
            closeResponse(response);
        }
    }

    /**
     * 以表单形式执行 GET 请求
     * @param url       请求url
     * @param header    请求头部信息
     * @param params    请求参数
     */
    @Override
    public String get(String url, Map<String, String> header, Map<String,String> params) throws Exception {
        CloseableHttpResponse response = null;
        HttpGet httpGet = null;
        try {
            //创建HttpClient对象，HttpClients.createDefault()
            CloseableHttpClient httpClient = HttpClients.createDefault();
            //构建url中的请求参数
            url = url + toHttpGetParams(params);
            //基于要发送的HTTP请求类型创建HttpGet或者HttpPost实例
            httpGet = new HttpGet(url);
            //get请求，添加header
            setHeaderAndConfig(header,httpGet,ContentTypeEnum.BODY);
            //发起请求
            logger.info(String.format("请求URL: %s",url));
            response = httpClient.execute(httpGet);
            return getResponse(response);
        }finally {
            //关闭连接
            closeResponse(response);
        }
    }

    private String getResponse(CloseableHttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        String result = EntityUtils.toString(entity, "utf-8");
        EntityUtils.consume(entity);
        return result;
    }

    /**
     * 设置头部信息
     * @param header            头部信息
     * @param httpRequestBase   请求对象
     * @param ContentType       请求体方式
     */
    private void setHeaderAndConfig(Map<String, String> header, HttpRequestBase httpRequestBase, ContentTypeEnum ContentType){
        //  设置默认请求方式
        switch (ContentType){
            case BODY:
                httpRequestBase.addHeader("Content-Type", ContentTypeEnum.BODY.getValue());
                break;
            case FORM:
                httpRequestBase.addHeader("Content-Type", ContentTypeEnum.FORM.getValue());
                break;
            default:
                logger.error("默认设置请求头部信息失败");
        }
        //  设置头部信息
        if (header != null && !header.isEmpty()) {
            for (Map.Entry<String, String> entry : header.entrySet()) {
                httpRequestBase.addHeader(entry.getKey().trim(), entry.getValue().trim());
            }
        }
        //  设置连接超时信息
        httpRequestBase.setConfig(getRequestConfig());
    }

    /**
     * 这里只是其中的一种场景,也就是把参数用&符号进行连接且进行URL编码
     * 根据实际情况拼接参数
     */
    public String toHttpGetParams(Map<String, String> urlParams) throws Exception {
        if (urlParams == null || urlParams.isEmpty()) {
            return "";
        }
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("?");
        for (Map.Entry<String, String> entry : urlParams.entrySet()) {
            stringBuffer.append(entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), "utf-8") + "&");
        }
        stringBuffer.delete(stringBuffer.lastIndexOf("&"),stringBuffer.length());
        return stringBuffer.toString();
    }

    /**
     * 设置连接超时信息
     * @return
     */
    private RequestConfig getRequestConfig(){
        /*设置连接超时
        setConnectTimeout：设置连接超时时间，单位毫秒。
        setConnectionRequestTimeout：设置从connect Manager(连接池)获取Connection 超时时间，单位毫秒。这个属性是新加的属性，因为目前版本是可以共享连接池的。
        setSocketTimeout：请求获取数据的超时时间(即响应时间)，单位毫秒。 如果访问一个接口，多少时间内无法返回数据，就直接放弃此次调用。
        */
        RequestConfig requestConfig = RequestConfig.custom()
                .setConnectTimeout(5000)
                .setConnectionRequestTimeout(1000)
                .setSocketTimeout(5000)
                .build();
        return  requestConfig;
    }

    /**
     * 关闭连接
     * @param response
     */
    private void closeResponse(CloseableHttpResponse response){
        if (response != null) {
            try {
                response.close();
                //不可以关闭，不然连接池就会被关闭
                //httpclient.close();
            } catch (IOException e) {
                logger.error( "httpClient关闭连接失败, " + e);
            }
        }
    }
}
