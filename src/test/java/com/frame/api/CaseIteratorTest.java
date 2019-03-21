package com.frame.api;

import com.frame.annotations.CaseSource;
import com.frame.annotations.ReadyData;
import com.frame.annotations.ReadyTestData;
import com.frame.base.BaseTest;
import com.frame.config.AbstractTestBase;
import com.frame.util.DiffResultUtil;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

public class CaseIteratorTest extends BaseTest {

    @CaseSource(path = "testcase/DeletePurchaseTest.csv")
    @Test(dataProvider = "autoDataProvider")
    public void test_1(Map<String, String> data){
        Map<String, String> map = baseConfig;
        for (Map.Entry entry : data.entrySet()){
//            System.err.println(entry.getKey() + " === " + entry.getValue());
        }
    }

    @CaseSource(path = "testcase/DeletePurchaseTest.xls",type = "excel")
    @ReadyTestData(datas = {@ReadyData(tableName = "bill_transit",path = "dbdata/bill_transit.csv",dbName = "supply_scmdb_db")})
    @Test(dataProvider = "autoDataProvider")
    public void test_2(Map<String, String> data){
        Map<String,Map<String, List<Map<String,Object>>>> mapMap = AbstractTestBase.currentTestReadyDBData;
        System.err.println(mapMap.size());
        Map<String, String> map = baseConfig;
        for (Map.Entry entry : data.entrySet()){
//            System.err.println(entry.getKey() + " === " + entry.getValue());
        }
    }

    @Test
    public void test_3(){
        String expect = "{\"entityId\":\"99228524\",\"selfEntityId\":\"99228525\",\"warehouseId\":\"\",\"detail\":[{\"isContainDetails\":\"2\",\"pageNo\":1,\"pageSize\":50,\"cs\":{\"count\":\"1\"}},{\"hh\":\"0\",\"zz\":1,\"cc\":50,\"cs\":{\"count\":\"1\"}}]}";
        String actual = "{\"entityId\":\"99228524\",\"selfEntityId\":\"99228525\",\"warehouseId\":\"\",\"detail\":[{\"isContainDetails\":\"2\",\"pageNo\":1,\"pageSize\":50,\"cs\":{\"count\":\"1\"}},{\"hh\":\"0\",\"zz\":1,\"cc\":50,\"cs\":{\"count\":\"1\"}}]}";
        DiffResultUtil.diffResponse(expect,actual,null);
    }

    @Test
    public void test_4(){
        String expect = "{\"entityId\":\"99228524\",\"selfEntityId\":\"99228525\",\"warehouseId\":\"\",\"detail\":[{\"isContainDetails\":\"2\",\"pageNo\":1,\"pageSize\":50,\"cs\":{\"count\":\"1\"}},{\"hh\":\"0\",\"zz\":1,\"cc\":50,\"cs\":{\"count\":\"1\"}},{\"isContainDetails\":\"3\",\"pageNo\":1,\"pageSize\":50,\"cs\":{\"count\":\"1\"}}]}";
        String actual = "{\"entityId\":\"99228524\",\"selfEntityId\":\"99228525\",\"warehouseId\":\"\",\"detail\":[{\"isContainDetails\":\"3\",\"pageNo\":1,\"pageSize\":50,\"cs\":{\"count\":\"1\"}},{\"hh\":\"0\",\"zz\":1,\"cc\":50,\"cs\":{\"count\":\"1\"}},{\"isContainDetails\":\"3\",\"pageNo\":1,\"pageSize\":50,\"cs\":{\"count\":\"1\"}}]}";
        DiffResultUtil.diffResponseIgnoreOrder(expect,actual,null);
    }

    @Test
    public void test_5(){
        String expect = "{\"entityId\":\"99228524\",\"selfEntityId\":\"99228525\",\"warehouseId\":\"\",\"detail\":[{\"isContainDetails\":\"2\",\"pageNo\":1,\"pageSize\":50,\"cs\":{\"count\":\"1\"}},{\"hh\":\"0\",\"zz\":1,\"cc\":50,\"cs\":{\"count\":\"1\"}}]}";
        String actual = "{\"entityId\":\"99228524\",\"selfEntityId\":\"99228525\",\"warehouseId\":\"\",\"detail\":[{\"isContainDetails\":\"3\",\"pageNo\":1,\"pageSize\":50,\"cs\":{\"count\":\"1\"}},{\"hh\":\"0\",\"zz\":1,\"cc\":50,\"cs\":{\"count\":\"1\"}}]}";
        DiffResultUtil.diffResponse(expect,actual,"detail");
    }
}
