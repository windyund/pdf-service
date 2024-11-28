package com.smart.neural.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.neural.constant.Constants;
import com.smart.neural.controller.vo.ReportMessage;
import com.smart.neural.util.FileUtils;
import com.smart.neural.util.OssUtils;
import com.smart.neural.util.ZipUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

@Slf4j
@Service
public class ReportService implements InitializingBean, DisposableBean {
    private static ExecutorService threadPoolExecutor;

    @Resource private ObjectMapper objectMapper;
    @Resource private ChromeService chromeService;
    @Resource private KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void afterPropertiesSet() {
        threadPoolExecutor = new ThreadPoolExecutor(
                4, 4, 1, TimeUnit.MINUTES,
                new ArrayBlockingQueue<>(100),
                new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Override
    public void destroy() {
        threadPoolExecutor.shutdown();
    }


    /**
     * 生成报告
     */
    public Boolean gen(ReportMessage msg) {
        log.info("""
                                                
                        ***************************************************
                        **** req_id:      {}
                        **** biz_id:      {}
                        **** biz_type:    {}
                        **** handler:     {}
                        **** reports:     {}
                        **** zip:         {}
                        **** zip_name:    {}
                        **** zip_oss_key: {}
                        ***************************************************
                        """,
                msg.getReqId(),
                msg.getBizId(),
                msg.getBizType(),
                msg.getHandler(),
                msg.getReports().size(),
                msg.isZip(),
                msg.getZipName(),
                msg.getZipOssKey());

        // 工作目录: /{report_dir}/{req_id}-{timestamp}
        String workspace = String.format("%s/%s-%s", Constants.REPORT_WORKDIR, msg.getReqId(), System.currentTimeMillis());
        try {
            // 创建工作目录
            File workspaceDir = new File(workspace);
            if (!workspaceDir.exists()) {
                if (!workspaceDir.mkdirs()) {
                    throw new RuntimeException("创建工作目录失败: " + workspace);
                }
            }

            if (Objects.equals(msg.getHandler(), "snapshot")) {
                // 创建截图临时目录: /{workspace}/img
                String imgDir = String.format("%s/img", workspace);
                File imgDirectory = new File(imgDir);
                if (!imgDirectory.exists()) {
                    if (!imgDirectory.mkdir()) {
                        throw new RuntimeException("创建截图临时目录失败: " + imgDir);
                    }
                }
            }

            // 创建报告临时目录: /{workspace}/pdf
            String pdfDir = String.format("%s/pdf", workspace);
            File pdfDirectory = new File(pdfDir);
            if (!pdfDirectory.exists()) {
                if (!pdfDirectory.mkdir()) {
                    throw new RuntimeException("创建报告临时目录失败: {}" + pdfDir);
                }
            }

            // 并发执行截图任务
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (ReportMessage.ReportParams reportParams : msg.getReports()) {
                SingleTask task = new SingleTask(chromeService, workspace, msg, reportParams);
                CompletableFuture<Void> future = CompletableFuture.runAsync(task, threadPoolExecutor);
                futures.add(future);
            }
            // 等待所有子任务完成
            futures.forEach(CompletableFuture::join);

            // 子任务有异常
            long count = futures.stream().filter(CompletableFuture::isCompletedExceptionally).count();
            if (count > 0) {
                msg.setResult(false);
                msg.setMessage("生成报告失败!");
                return msg.isResult();
            }

            // 打包
            if (Objects.equals(true, msg.isZip())) {
                // zip路径: /{workspace}/{zip_name}
                String zipFile = String.format("%s/%s", workspace, msg.getZipName());
                ZipUtils.zip(pdfDir, zipFile);
                // 上传压缩包
                OssUtils.upload(zipFile, msg.getZipOssKey(), msg.getZipName());
            }

            msg.setResult(true);
            msg.setMessage("success");
        } catch (Exception e) {
            log.error("生成报告失败: {}", e.getMessage(), e);
            msg.setResult(false);
            msg.setMessage(e.getMessage());
        } finally {
            // 删除临时目录
            try {
                FileUtils.deleteFile(workspace);
            } catch (IOException e) {
                log.error("删除临时目录失败: {}", workspace);
            }
            // 回调处理
            this.notify(msg);
            log.info("报告请求完成: {}", msg.getReqId());
            return msg.isResult();
        }
    }


    private void notify(ReportMessage msg) {
        try {
            String message = objectMapper.writeValueAsString(msg);
            if (msg.getNotify() != null) {
                ReportMessage.Notify notify = msg.getNotify();
                switch (notify.getType()) {
                    case "kafka":
                        kafkaTemplate.send(Constants.TOPIC_SMART_PDF_NOTIFY, message);
                        break;
                    case "http":
                        // todo http
                        break;
                }
            }
            log.info("回调：{}", message);
        } catch (JsonProcessingException e) {
            log.error("回调消息错误： {}", msg);
        } catch (Exception e) {
            log.error("回调失败：{}", e.getMessage(), e);
        }
    }

}
