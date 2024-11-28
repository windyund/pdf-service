package com.smart.neural.util;

import com.itextpdf.text.PageSize;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.BoundingBox;
import com.microsoft.playwright.options.Margin;
import com.microsoft.playwright.options.ScreenshotType;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.smart.neural.constant.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.Asserts;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;

/**
 * 截图工具类
 */
@Slf4j
public class BrowserUtils {

    /**
     * 生成矢量pdf
     *
     * @param browser 浏览器对象
     * @param url     访问地址
     * @param pdfPath pdf路径
     */
    public static void pdf(BrowserContext browser, String url, String pdfPath) {
        Asserts.notNull(browser, "browser is null");
        Asserts.notBlank(url, "url is null");
        Asserts.notBlank(pdfPath, "pdfPath is null");
        Page page = null;
        try {
            page = browser.newPage();
            page.navigate(url);
            // 等待渲染完成，最多等待20秒
            Page.WaitForSelectorOptions waitOptions = new Page.WaitForSelectorOptions()
                    .setTimeout(Constants.WAIT_TIME_MS)
                    .setState(WaitForSelectorState.ATTACHED);
            page.waitForSelector(Constants._done_selector, waitOptions);
            Page.PdfOptions pdfOptions = new Page.PdfOptions().setPath(Paths.get(pdfPath));
            // 设置页面样式，去掉边框
            String style = "body { margin: 0; padding: 0; border: 0; }";
            page.addStyleTag(new Page.AddStyleTagOptions().setContent(style));
            //打印背景色
            pdfOptions.setPrintBackground(true);
            //A4大小
            pdfOptions.setWidth(String.valueOf(PageSize.A4.getWidth()));
            pdfOptions.setHeight(String.valueOf(PageSize.A4.getHeight()));
            //页边距
            Margin margin = new Margin();
            margin.setBottom("0");
            margin.setTop("0");
            margin.setLeft("0");
            margin.setRight("0");
            pdfOptions.setMargin(margin);
            page.pdf(pdfOptions);
        } catch (Exception ex) {
            log.error("browser pdf error: ", ex);
            throw new RuntimeException(ex);
        } finally {
            if (page != null) {
                page.close();
            }
        }
    }

    /**
     * 截图生成到指定目录
     *
     * @param browserContext 浏览器对象
     * @param url     访问地址
     * @param imgDir  截图目录
     */
    public static void screenshot(BrowserContext browserContext, String url, String imgDir) {
        Asserts.notNull(browserContext, "browserContext is null");
        Asserts.notBlank(url, "url is null");
        Asserts.notBlank(imgDir, "imgDir is null");
        Page page = null;
        try {
            page = browserContext.newPage();
            page.navigate(url);
            // 等待渲染完成，最多等待20秒
            Page.WaitForSelectorOptions waitOptions = new Page.WaitForSelectorOptions()
                    .setTimeout(Constants.WAIT_TIME_MS)
                    .setState(WaitForSelectorState.ATTACHED);
            page.waitForSelector(Constants._done_selector, waitOptions);

            // 获取容器大小，设置viewport
            ElementHandle contents = page.querySelector(Constants._content_selector);
            BoundingBox boundingBox = contents.boundingBox();
            page.setViewportSize(SafeConverter.toInt(boundingBox.width), SafeConverter.toInt(boundingBox.height));
            File imgDirectory = new File(imgDir);
            if (!imgDirectory.exists()) {
                if (!imgDirectory.mkdirs()) {
                    log.error("创建截图临时目录失败: {}", imgDir);
                    throw new RuntimeException("创建截图临时目录失败!");
                }
            }
            // 获取所有page页面
            List<ElementHandle> elementHandles = contents.querySelectorAll(Constants._page_selector);
            for (int i = 0; i < elementHandles.size(); i++) {
                ElementHandle elementHandle = elementHandles.get(i);
                // 截图
                String imgPath = String.format("%s/%s.png", imgDir, i);
                ElementHandle.ScreenshotOptions screenshotOptions = new ElementHandle.ScreenshotOptions()
                        .setPath(Paths.get(imgPath))
                        .setType(ScreenshotType.PNG);
                BoundingBox boundingBox1 = elementHandle.boundingBox();
                page.setViewportSize(SafeConverter.toInt(boundingBox1.width), SafeConverter.toInt(boundingBox1.height));
                elementHandle.screenshot(screenshotOptions);
            }
        } catch (Exception ex) {
            log.error("screenshot error: {}", ex.getMessage(), ex);
            log.error("screenshot error url: {}", url);
            throw new RuntimeException(ex);
        } finally {
            if (page != null) {
                page.close();
            }
        }
    }
}
