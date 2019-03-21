package com.frame.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 字符串转其他类型工具类
 */
public class StringHelper {
    /**
     * json字符串转bean对象
     * @param jsonStr
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T covToVo(String jsonStr, Class<T> clazz){
        if (jsonStr == null || "null".equalsIgnoreCase(jsonStr.trim())){
            return null;
        }
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        return JSONObject.toJavaObject(jsonObject,clazz);
    }

    /**
     * json字符串转bean对象List
     * @param jsonStr
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> List<T> covToVoList(String jsonStr, Class<T> clazz){
        if (jsonStr == null || "null".equalsIgnoreCase(jsonStr.trim())){
            return null;
        }
        JSONArray jsonArray = JSONObject.parseArray(jsonStr);
        List<T> list = new ArrayList<T>();
        for (int i=0; i<jsonArray.size(); i++){
            list.add(JSONObject.toJavaObject(jsonArray.getJSONObject(i),clazz));
        }
        return list;
    }

    /**
     * 转字符串
     * @param str
     * @return
     */
    public static String covToString(String str){
        if ("null".equalsIgnoreCase(str.trim())){
            return null;
        }
        return str;
    }

    /**
     * 字符串转boolean
     * @param str
     * @return
     */
    public static Boolean covToBoolean(String str){
        if (StringUtils.isBlank(str) || "false".equalsIgnoreCase(str.trim())){
            return false;
        }
        return true;
    }
    /**
     * 字符串转list，以分号分隔
     * @param str
     * @return
     */
    public static List<String> covToList(String str){
        if (str == null || "null".equalsIgnoreCase(str.trim())){
            return null;
        }
        List<String> ids = new ArrayList<String>();
        if ("".equals(str.trim())){
            return ids;
        }
        String[] strs = str.split(";");
        ids.addAll(Arrays.asList(strs));
        return ids;
    }

    /**
     * 字符串转int
     * @param str
     * @return
     */
    public static Integer covToInteger(String str){
        if (StringUtils.isBlank(str) || "null".equalsIgnoreCase(str.trim())){
            return null;
        }
        return Integer.parseInt(str);
    }
}
