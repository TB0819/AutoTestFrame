package com.frame.server;

import java.io.IOException;
import java.util.Map;

public interface HttpService {
    public String postWithBody(String url, Map<String,String> header, String body) throws IOException;

    public String postWithForm(String url, Map<String,String> header, Map<String,String> params) throws IOException;

    public String get(String url, Map<String,String> header, Map<String,String> params) throws Exception;
}
