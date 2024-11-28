package com.smart.neural.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.neural.constant.Constants;
import com.smart.neural.controller.vo.ReportMessage;
import com.smart.neural.service.ReportService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * 处理实时生成pdf请求
 * 队列隔离：实时任务与批次任务分开处理，避免批次任务阻塞实时任务，会竞争线程池资源，抢占式执行
 */
@Slf4j
@Service
public class RealTimeProcessReportListener {
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private ReportService reportService;


    @KafkaListener(topics = Constants.TOPIC_SMART_PDF_REALTIME_REQUEST)
    public void listen(String message) {
        log.debug("收到消息: {}", message);
        try {
            ReportMessage msg = objectMapper.readValue(message, ReportMessage.class);
            reportService.gen(msg);
        } catch (JsonProcessingException e) {
            log.error("消息格式错误：{}", message);
        }
    }

}
