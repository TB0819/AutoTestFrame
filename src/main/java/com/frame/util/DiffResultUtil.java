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
public class DiffResultUtil {
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
