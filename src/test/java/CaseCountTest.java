import com.frame.annotations.CaseSource;
import com.frame.base.BaseTest;
import com.frame.server.imp.HttpServiceImp;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class CaseCountTest extends BaseTest {

    @CaseSource
    @Test(dataProvider = "autoDataProvider")
    public void test_1(Map<String, String> data){
        Map<String, String> map = baseConfig;
        for (Map.Entry entry : data.entrySet()){
//            System.err.println(entry.getKey() + " === " + entry.getValue());
        }
    }

    @CaseSource(type = "excel")
    @Test(dataProvider = "autoDataProvider")
    public void test_2(Map<String, String> data){
        Map<String, String> map = baseConfig;
        for (Map.Entry entry : data.entrySet()){
//            System.err.println(entry.getKey() + " === " + entry.getValue());
        }
    }

    @Test
    public void test_3() throws Exception {
        HttpServiceImp httpServiceImp = new HttpServiceImp();
        Map<String, String> map = new HashMap<>();
        map.put("name","cangzhu");
        map.put("age","23");
        map.put("sex","man");
        String str = httpServiceImp.toHttpGetParams(map);
        System.err.println(str);
    }
}
