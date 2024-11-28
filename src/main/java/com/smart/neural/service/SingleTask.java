package com.smart.neural.service;

import com.microsoft.playwright.BrowserContext;
import com.smart.neural.controller.vo.ReportMessage;
import com.smart.neural.util.BrowserUtils;
import com.smart.neural.util.OssUtils;
import com.smart.neural.util.PdfUtils;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

@Slf4j
public class SingleTask implements Runnable {
    private final ChromeService chromeService;
    private final String workspace;
    private final ReportMessage reportMessage;
    private final ReportMessage.ReportParams params;

    public SingleTask(ChromeService chromeService, String workspace,ReportMessage reportMessage, ReportMessage.ReportParams params) {
        this.chromeService = chromeService;
        this.workspace = workspace;
        this.reportMessage = reportMessage;
        this.params = params;
    }

    @Override
    public void run() {
        log.info("子线程开始执行：{}", params.getReportName());
        BrowserContext browserContext = null;
        long start = System.currentTimeMillis();
        try {
            browserContext = chromeService.getBrowserContext();
            log.debug("acquire browserContext");
            // pdf目录
            String pdfDir = String.format("%s/pdf/%s", workspace, params.getReportPath());
            File pdfDirectory = new File(pdfDir);
            if (!pdfDirectory.exists()) {
                try {
                    if (!pdfDirectory.mkdirs()) {
                        // 检查目录是否已被另一个线程创建
                        if (!pdfDirectory.exists()) {
                            log.error("创建pdf临时目录失败：{}", pdfDir);
                            throw new RuntimeException("创建pdf临时目录失败!");
                        }
                    }
                } catch (SecurityException e) {
                    // 再次检查目录是否存在
                    if (!pdfDirectory.exists()) {
                        log.error("创建pdf临时目录失败 (SecurityException)：{}", pdfDir, e);
                        throw new RuntimeException("创建pdf临时目录失败 (SecurityException)!");
                    }
                }
            }
            String pdfPath = String.format("%s/%s", pdfDir, params.getReportName());
            switch (reportMessage.getHandler()) {
                case "snapshot":
                    // 1.截图目录: /{workspace}/img/校级/{dimension_id}
                    String imgDir = String.format("%s/img/%s/%s", workspace, params.getReportPath(), params.getReportId());
                    BrowserUtils.screenshot(browserContext, params.getReportUrl(), imgDir);
                    log.debug("screenshot cost: {}", System.currentTimeMillis() - start);
                    // 2.生成pdf
                    PdfUtils.imagesToPdf(imgDir, pdfPath);
                    break;
                case "pdf":
                    BrowserUtils.pdf(browserContext, params.getReportUrl(), pdfPath);
                    break;
            }
            // 3.上传oss
            OssUtils.upload(pdfPath, params.getOssKey(), params.getReportName());
        } catch (Exception e) {
            log.error("报告子任务失败：{}", e.getMessage(), e);
            throw new RuntimeException(e);
        } finally {
            if (browserContext != null) {
                chromeService.returnBrowserContext(browserContext);
                log.debug("release browserContext");
            }
            log.info("子线程执行完成：{}, 耗时：{}ms", this.params.getReportName(), System.currentTimeMillis() - start);
        }
    }

}
