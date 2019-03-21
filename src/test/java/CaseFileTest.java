import com.frame.annotations.CaseSource;
import com.frame.base.BaseTest;
import com.frame.config.ContentTypeEnum;
import org.testng.annotations.Test;

import java.util.Map;


public class CaseFileTest extends BaseTest {

    @CaseSource(path = "C:\\workspace\\DeletePurchaseTest.csv")
    @Test(dataProvider = "autoDataProvider")
    public void test_1(Map<String, String> data){
        Map<String, String> map = baseConfig;
        for (Map.Entry entry : data.entrySet()){
//            System.err.println(entry.getKey() + " === " + entry.getValue());
        }
    }

    @CaseSource(path = "C:\\workspace\\DeletePurchaseTest.xls",type = "excel")
    @Test(dataProvider = "autoDataProvider")
    public void test_2(Map<String, String> data){
        Map<String, String> map = baseConfig;
        for (Map.Entry entry : data.entrySet()){
            System.err.println(entry.getKey() + " === " + entry.getValue());
        }
    }

    @Test
    public void test_3(){
        ContentTypeEnum ContentType = ContentTypeEnum.BODY;
        switch (ContentType){
            case BODY:
                System.err.println(ContentTypeEnum.BODY.getValue());
                break;
            case FORM:
                System.err.println(ContentTypeEnum.FORM.getValue());
        }
    }

}
