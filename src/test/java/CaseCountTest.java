import com.frame.annotations.CaseSource;
import com.frame.base.BaseTest;
import org.testng.annotations.Test;

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
}
