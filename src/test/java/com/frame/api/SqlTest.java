package com.frame.api;

import com.frame.annotations.ReadyData;
import com.frame.annotations.ReadyTestData;
import com.frame.base.BaseTest;
import com.frame.config.AbstractTestBase;
import com.frame.server.DBServer;
import com.frame.server.imp.DBServerImp;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

public class SqlTest extends BaseTest {

    @Test
    public void test_1() throws Exception {
        DBServer dbServer = new DBServerImp();
        List<Map<String, Object>> result = dbServer.querySql("supply_scmdb_db","select * from warehouse where entity_id='99228526' and self_entity_id='99228526'");
        System.err.println(result.size());
    }

    @Test
    public void test_2() throws Exception {
        DBServer dbServer = new DBServerImp();
        List<String[]> list = dbServer.getFieldMetaData("supply_scmdb_db","warehouse");
        System.err.println(list.size());
    }

    @Test
    public void test_3() throws Exception {
        DBServerImp dbServer = new DBServerImp();
//        List<String> list = dbServer.getInsertSqlList("supply_scmdb_db","C:\\workspace\\testwork\\AutoTestFrame\\src\\test\\resources\\dbdata\\bill_transit.csv","bill_transit");
        dbServer.insertFromFile("supply_scmdb_db","dbdata/bill_transit.csv","bill_transit");
//        System.err.println(list.size());
    }

    @Test
    public void test_4() throws Exception{
        DBServerImp dbServer = new DBServerImp();
        dbServer.deleteFromFile("supply_scmdb_db","dbdata\\bill_transit.csv","bill_transit",null);
    }

    @ReadyTestData(datas = {@ReadyData(tableName = "bill_transit",path = "dbdata/bill_transit.csv",dbName = "supply_scmdb_db"),
            @ReadyData(tableName = "supplier",path = "dbdata/supplier.csv",dbName = "supply_scmdb_db")})
    @Test
    public void test_5() {
        Map<String,Map<String, List<Map<String,Object>>>> mapMap = AbstractTestBase.currentTestReadyDBData;
        System.err.println(mapMap.size());
    }

    @ReadyTestData(datas = {@ReadyData(tableName = "bill_transit",path = "dbdata/bill_transit.csv",dbName = "supply_scmdb_db")})
    @Test
    public void test_6() {
        Map<String,Map<String, List<Map<String,Object>>>> mapMap = AbstractTestBase.currentTestReadyDBData;
        System.err.println(mapMap.size());
    }

}
