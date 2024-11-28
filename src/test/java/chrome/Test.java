package chrome;

import com.microsoft.playwright.*;
import com.smart.neural.constant.Constants;
import com.smart.neural.util.BrowserUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class Test {

    public static BrowserType.LaunchOptions getLaunchOptions() {
        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions();
        launchOptions.setArgs(Arrays.asList(
                "--disable-gpu",
                "--disable-dev-shm-usage",
                "--disable-setuid-sandbox",
                "--no-first-run",
                "--no-zygote",
                "--disable-web-security",
                "--no-sandbox"
        ));

        launchOptions.setTimeout(0);
        launchOptions.setHeadless(false);
        launchOptions.setHandleSIGHUP(false);
        launchOptions.setHandleSIGINT(false);
        launchOptions.setHandleSIGTERM(false);
        return launchOptions;
    }


    public static void testScreenshot() {
        long start = System.currentTimeMillis();
        String url ="https://smartreport.17zuoye.com/pr/opreport/#/?id=TERM-1691596800-SCHOOL-506282&appKey=SmartTest&ts=1705369922027&sig=bb06184678f7a86b8345f7b27557a3b8";
        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(getLaunchOptions());
        try {
            // 3840 x 2160
            Browser.NewContextOptions newContextOptions = new Browser.NewContextOptions().setViewportSize(1920, 1080);
            newContextOptions.setDeviceScaleFactor(2.0);
            BrowserContext browserContext = browser.newContext(newContextOptions);
            String workdir = String.format("%s/%s", Constants.REPORT_WORKDIR, "test_003");
            // 创建截图临时目录: /{workdir}/img/校级
            String imgDir = String.format("%s/img/%s", workdir, "区级");
            BrowserUtils.screenshot(browserContext, url, imgDir);
        } catch (Exception e) {
            log.error("生成报告失败: {}", e.getMessage(), e);
        } finally {
            browser.close();
            playwright.close();
        }
        System.out.println("耗时: " + (System.currentTimeMillis() - start) + "ms");
    }

    public static void testPdf() {
        long start = System.currentTimeMillis();
        String url = "https://smartreport.test.17zuoye.net/pr/xcjc/#/?examId=61d6c85890750568271256c7&studentId=16083004&appKey=SmartTest&sig=1111";
        Playwright playwright = Playwright.create();
        Browser browser = playwright.chromium().launch(getLaunchOptions());
        try {
            Browser.NewContextOptions newContextOptions = new Browser.NewContextOptions().setViewportSize(1920, 1080);
            newContextOptions.setDeviceScaleFactor(2.0);
            BrowserContext browserContext = browser.newContext(newContextOptions);
//            BrowserContext browserContext = browser.newContext();
            String workdir = String.format("%s/%s", Constants.REPORT_WORKDIR, "test");
            // pdf生成目录: /{workdir}/pdf/test.pdf
            String pdfPath = String.format("%s/pdf/%s", workdir, "test.pdf");
            BrowserUtils.pdf(browserContext, url, pdfPath);
        } catch (Exception e) {
            log.error("生成报告失败: {}", e.getMessage(), e);
        } finally {
            browser.close();
            playwright.close();
        }
        System.out.println("耗时: " + (System.currentTimeMillis() - start) + "ms");
    }










    @Data
    public static class InputModel {

        public InputModel(String projectId, List<Ac> acs) {
            this.projectId = projectId;
            Acs = acs;
            if(!CollectionUtils.isEmpty(acs)) {
                acs.stream().collect(Collectors.summarizingDouble(e->e.getValue()));
            }
        }

        private String projectId;

        private List<Ac> Acs;

        private Double value;


    }
    @Data
    public static class OutputModel{
        private String Ac;
        private List<Project> projects;
        private Double value;
    }
    @Data
    public static class Project{
        private String projectName;
        private Double value;
    }

    @Data
    @AllArgsConstructor
    public static class Ac {
        private String name;
        private Double value;
    }


    //转换
    public static void convert(List<InputModel> models) {
        if(CollectionUtils.isEmpty(models)) {
            return;
        }
    }


    public static void main(String[] args) {
        InputModel p1 = new InputModel("PN001", Arrays.asList(new Ac("AC001", 0.20), new Ac("AC002", 0.20)));

        InputModel p3 = new InputModel("PN003", Arrays.asList(new Ac("AC001", 0.15), new Ac("AC002", 0.20)));

        InputModel p4 = new InputModel("PN004", Arrays.asList(new Ac("AC001", 0.30)));
    }

}
