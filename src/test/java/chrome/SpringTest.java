package chrome;

import com.smart.neural.Application;
import com.smart.neural.util.OssUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = Application.class)
public class SpringTest {


    @Test
    public void  testUpload() {
        // 获取当前项目路径
        String projectPath = System.getProperty("user.dir");
        String filePath = projectPath + "/report/test_001/pdf/区级/全区报告.pdf";
        String ossKey = "test/exam/report/2023/test.pdf";

        long start = System.currentTimeMillis();
        OssUtils.upload(filePath, ossKey, "测试中文名称.pdf");
        System.out.println("cost: " + (System.currentTimeMillis() - start) + "ms");
    }
}
