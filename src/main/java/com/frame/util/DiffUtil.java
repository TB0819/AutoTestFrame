package com.frame.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import java.util.Iterator;
import java.util.Map;

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

    public static void removeField(Object json, List<String> fields) {
        if (json instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) json;
            for (int i=0; i< jsonArray.size(); i++) {
                removeField(jsonArray.get(i), fields);
            }
        } else if (json instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) json;
            for (String field : fields) {
                jsonObject.remove(field);
            }
            Set<String> keys = jsonObject.keySet();
            for (String key: keys) {
                removeField(jsonObject.get(key), fields);
            }
        }
    }
    
    /**
     * 修改、删除操作后的db数据全字段对比，根据准备数据和预期结果json组合与实际结果对比
     * @param expectJson        预期结果字段json
     * @param actualData        实际DB数据
     * @param readyData         准备DB数据
     */
    public static void diffDbToAllField(String expectJson, Map<String, Object> actualData, Map<String, Object> readyData) {
        if (actualData.isEmpty() || actualData == null) {
            Assert.fail("实际结果为空或null!");
        }
        Map expectData = JSON.parseObject(expectJson,Map.class);
        for (Map.Entry<String, Object> entry: actualData.entrySet()){
            String field = entry.getKey();
            if (readyData.get(field).toString().indexOf("()") > 0){
                //  准备数据的值为函数时，则判断是不是空
                Assert.assertTrue( entry.getValue()!= null,field + "结果为null");
            } else if (expectData.containsKey(field)) {
                Assert.assertEquals(entry.getValue(), expectData.get(field),field + "结果不一致");
            } else {
                Assert.assertEquals(entry.getValue(), readyData.get(field),field + "结果不一致");
            }
        }
    }

    /**
     * DB数据对比，预期结果为json，实际结果为查询结果,只校验预期字段
     * @param expectJson    预期结果
     * @param actualData    实际结果
     */
    public static void diffDbToExpectField(String expectJson, Map<String, Object> actualData){
        Map expectData = JSON.parseObject(expectJson,Map.class);
        for (Object key : expectData.keySet()) {
            String field = String.valueOf(key);
            Assert.assertEquals(actualData.get(field), expectData.get(field),field + "结果不一致");
        }
    }

    /**
     * response 结果校验,字段按预期结果来对比
     * @param expectJson    预期结果
     * @param actualJson    实际结果
     * @param startKey      起始字段
     */
    public static void diffResponse(String expectJson, String actualJson,String startKey){
        JSONObject expect = JSON.parseObject(expectJson);
        JSONObject actual = JSON.parseObject(actualJson);
        try {
            if (StringUtils.isBlank(startKey)) {
                compareJson(expect,actual,null,false);
            } else {
                compareJson(expect.get(startKey),actual.get(startKey),startKey,false);
            }
        }catch (Exception e){
            Assert.fail(e.getMessage());
        }
    }

    /**
     * response 结果校验,字段按预期结果来对比，忽略数组顺序
     * @param expectJson        预期结果
     * @param actualJson        实际结果
     * @param startKey          起始字段
     */
    public static void diffResponseIgnoreOrder(String expectJson, String actualJson,String startKey){
        JSONObject expect = JSON.parseObject(expectJson);
        JSONObject actual = JSON.parseObject(actualJson);
        try {
            if (StringUtils.isBlank(startKey)) {
                compareJson(expect,actual,null,true);
            } else {
                compareJson(expect.get(startKey),actual.get(startKey),startKey,true);
            }
        }catch (Exception e){
            Assert.fail(e.getMessage());
        }
    }

    // 两个JSONObject对比
    private static void compareJson(JSONObject expectJson, JSONObject actualJson, String key, boolean Ignore) throws Exception {
        Iterator<String> iterator = expectJson.keySet().iterator();
        while (iterator.hasNext()){
            String temp = iterator.next();
            if (!actualJson.containsKey(temp)) {
                throw new Exception(String.format(key.replaceAll("null","首节点") + " 结果不一致\n 实际值不存在： %s",temp));
            }
            compareJson(expectJson.get(temp), actualJson.get(temp), key + "-> " +temp,Ignore);
        }
    }
    // 两个Object对比
    private static void compareJson(Object expectJson, Object actualJson, String key, boolean Ignore) throws Exception {
        if (expectJson == null){
            if (null != actualJson)
                throw new Exception(String.format(key.replaceAll("null","首节点") + " 结果不一致\n 预期值：%s\n 实际值：%s \n",null,actualJson));
        } else if (expectJson instanceof JSONArray){
            compareJson((JSONArray) expectJson, (JSONArray)actualJson, key,Ignore);
        } else if (expectJson instanceof JSONObject){
            compareJson((JSONObject) expectJson, (JSONObject)actualJson, key,Ignore);
        }else {
            if (!expectJson.equals(actualJson) ) {
                throw new Exception(String.format(key.replaceAll("null","首节点") + " 结果不一致\n 预期值：%s\n 实际值：%s",String.valueOf(expectJson),String.valueOf(actualJson)));
            }
        }
    }
    // 两个JSONArray对比
    private static void compareJson(JSONArray expectJson, JSONArray actualJson, String key, boolean Ignore) throws Exception {
        for (int i=0; i<expectJson.size(); i++) {
            if (Ignore){
                for (int j =0; j< actualJson.size(); j++){
                    try {
                        compareJson(expectJson.get(i), actualJson.get(j),key + " 第[ " + (i+1) +" ]项",Ignore);
                        break;
                    }catch (Exception e){
                        if (j >= actualJson.size()-1){
                            Exception exception = new Exception(key + " 第" + (i+1) +"项 未找到对应的值\n" + expectJson.getString(i));
                            exception.initCause(e);
                            throw exception;
                        }
                    }
                }
            } else {
                compareJson(expectJson.get(i), actualJson.get(i),key + " 第[ " + (i+1) +" ]项",Ignore);
            }
        }
    }
}
