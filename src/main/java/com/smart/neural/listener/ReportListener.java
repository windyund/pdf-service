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

@Slf4j
@Service
public class ReportListener {
    @Resource private ObjectMapper objectMapper;
    @Resource private ReportService reportService;

    @KafkaListener(topics = Constants.TOPIC_SMART_PDF_REQUEST)
    public void listen(String message) {
        try {
            log.debug("收到消息: {}", message);
            ReportMessage msg = objectMapper.readValue(message, ReportMessage.class);
            reportService.gen(msg);
        } catch (JsonProcessingException e) {
            log.error("消息格式错误：{}", message);
        } catch (Exception e) {
            log.error("消息处理异常：{}", e.getMessage(), e);
        }
    }
}
