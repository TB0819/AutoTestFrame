package com.frame.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.util.*;

/**
 * 对比结果工具类
 */
public class DiffUtil {

    public static String diffJson(String expectedJsonStr, String actualJsonStr) throws Exception {
        return diffJson(expectedJsonStr, actualJsonStr, null, null, null);
    }

    public static String diffJson(String expectedJsonStr, String actualJsonStr, String extractJsonPath) throws Exception {
        return diffJson(expectedJsonStr, actualJsonStr, extractJsonPath, null, null);
    }

    public static String diffJson(String expectedJsonStr, String actualJsonStr, List<String> excludeJsonPaths) throws Exception {
        return diffJson(expectedJsonStr, actualJsonStr, null, null, excludeJsonPaths);
    }

    public static String diffJson(String expectedJsonStr, String actualJsonStr, String extractJsonPath, List<String> excludeFields, List<String> excludeJsonPaths) throws Exception {
        if (expectedJsonStr == null) {
            throw new Exception("Expected string is null");
        }else if (actualJsonStr == null) {
            throw new Exception("Actual string is null");
        } else if (expectedJsonStr.equals(actualJsonStr)) {
            return "预期值和实际值匹配正确";
        }
        Object expected = JSON.parse(expectedJsonStr);
        Object actual = JSON.parse(actualJsonStr);

        //  根据JsonPath提取指定的对比内容
        if (extractJsonPath != null && !extractJsonPath.isEmpty()) {
            //  根据JsonPath获取的内容进行对比
            if (!JSONPath.contains(expected,extractJsonPath)) {
                throw new Exception(String.format("Expected string not found jsonPath: %s", extractJsonPath));
            } else if (!JSONPath.contains(actual,extractJsonPath)) {
                throw new Exception(String.format("Actual string not found jsonPath: %s", extractJsonPath));
            }
            expected = JSONPath.eval(expected, extractJsonPath);
            actual = JSONPath.eval(actual, extractJsonPath);
        }

        //  移除符合JsonPath的属性
        if (excludeJsonPaths != null && !excludeJsonPaths.isEmpty()) {
            for (String path : excludeJsonPaths) {
                JSONPath.remove(expected, path);
                JSONPath.remove(actual, path);
            }
        }
        //  移除符合的字段属性
        if (excludeFields != null && !excludeFields.isEmpty()){
            removeField(expected, excludeFields);
            removeField(actual, excludeFields);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        compareJSON("", expected, actual, result);
        return JSONObject.toJSONString(result, SerializerFeature.WriteMapNullValue);
    }

    public static String diffJson(JSONObject expected, JSONObject actual) {
        List<Map<String, Object>> result = new ArrayList<>();
        compareJSON("", expected, actual, result);
        return JSONObject.toJSONString(result);
    }

    public static String diffJson(JSONArray expected, JSONArray actual) {
        List<Map<String, Object>> result = new ArrayList<>();
        compareJSON("", expected, actual, result);
        return JSONObject.toJSONString(result);
    }

    private static void compareJSON(String prefix, Object expected, Object actual, List<Map<String, Object>> result) {
        if ((expected instanceof JSONObject) && (actual instanceof JSONObject)) {
            compareJSONObject(prefix, (JSONObject) expected, (JSONObject) actual, result);
        } else if ((expected instanceof JSONArray) && (actual instanceof JSONArray)) {
            compareJSONArray(prefix, (JSONArray) expected, (JSONArray) actual, result);
        } else {
            compareValue(prefix, expected, actual, result);
        }
    }

    private static void compareValue(String prefix, Object expectedValue, Object actualValue, List<Map<String, Object>> result) {
        if (expectedValue == null && actualValue == null) {
            return ;
        } else if (expectedValue != null && expectedValue.equals(actualValue)) {
            return ;
        }
        addError(result, prefix, "equals", expectedValue, actualValue);
    }

    private static void compareJSONArray(String prefix, JSONArray expected, JSONArray actual, List<Map<String, Object>> result) {
        // 预期或实际值某个为空时，结果则为错误
        if ((expected.size() < 1 || actual.size() < 1) && expected.size() != actual.size()) {
            addError(result, prefix, "equals", expected, actual);
            return;
        }
        for (int i = 0; i < expected.size(); i++) {
            String arrayPrefix = String.format("[%d]", i);
            compareJSON(qualify(prefix, arrayPrefix), expected.get(i), actual.get(i), result);
        }
    }

    private static void compareJSONObject(String prefix, JSONObject expected, JSONObject actual, List<Map<String, Object>> result) {
        Set<String> expectedKeys = expected.keySet();
        for (String key : expectedKeys) {
            //  预期的key在实际对象中不存在
            if (!actual.containsKey(key)) {
                addError(result, qualify(prefix, key), "missing", expected.get(key), actual.get(key));
                continue;
            }
            compareJSON(qualify(prefix, key), expected.get(key), actual.get(key), result);
        }
    }

    private static String qualify(String prefix, String key) {
        return "".equals(prefix) ? key : prefix + "." + key;
    }

    private static void addError(List<Map<String, Object>> result, String field, String type, Object expected, Object actual) {
        if (result == null) {
            return;
        }
        Map<String, Object> errResult = new HashMap<>();
        errResult.put("field", field);
        errResult.put("type", type);
        errResult.put("Expected", expected);
        errResult.put("Actual", actual);
        result.add(errResult);
    }

    private static void removeField(Object json, List<String> fields) {
        if (json instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) json;
            for (int i=0; i< jsonArray.size(); i++) {
                removeField(jsonArray.get(i), fields);
            }
        } else if (json instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) json;
            //  先移除符合的字段
            for (String field : fields) {
                jsonObject.remove(field);
            }
            //  再遍历剩下字段，继续下一层级的移除
            Set<String> keys = jsonObject.keySet();
            for (String key: keys) {
                removeField(jsonObject.get(key), fields);
            }
        }
    }

    public static String diffDBData(String expectJsonStr, Map<String, Object> actualDbData) throws Exception {
        String actualDbJson = JSON.toJSONString(actualDbData, SerializerFeature.WriteMapNullValue);
        return diffJson(expectJsonStr, actualDbJson);
    }
    
    public static String diffDBData(String expectJsonStr, List<Map<String, Object>> actualDbList) throws Exception {
        String actualDbJson = JSON.toJSONString(actualDbList, SerializerFeature.WriteMapNullValue);
        return diffJson(expectJsonStr, actualDbJson);
    }
    
    public static String diffDBData(String expectJsonStr, Map<String, Object> expectMap, Map<String, Object> actualMap) throws Exception {
        //  先移除值含有sql函数的对象
        for (Map.Entry<String, Object> entry: expectMap.entrySet()) {
            String field = entry.getKey();
            if (String.valueOf(entry.getValue()).indexOf("()") > 0){
                expectMap.remove(field, entry.getValue());
            }
        }
        //  合并
        Map map = JSON.parseObject(expectJsonStr, Map.class);
        if (map != null && !map.isEmpty()){
            expectMap.putAll(map);
        }
        String expectJson = JSON.toJSONString(expectMap);
        String actualJson = JSON.toJSONString(actualMap);
        return diffJson(expectJson, actualJson);
    }

}
