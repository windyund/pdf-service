package com.smart.neural.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.smart.neural.constant.Constants;
import com.smart.neural.controller.vo.ReportMessage;
import com.smart.neural.controller.vo.Result;
import com.smart.neural.service.ReportService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class IndexController {
    @Resource private ReportService reportService;
    @Resource private ObjectMapper objectMapper;
    @Resource private KafkaTemplate<String, String> kafkaTemplate;

    @GetMapping("/")
    public String get() {
        return "Hello Neuron!";
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @PostMapping("/pdf")
    public Result<Boolean> pdf(@RequestBody @Valid ReportMessage request) {
        log.info("收到请求: {}", request.getReqId());
        try {
            String message = objectMapper.writeValueAsString(request);
            kafkaTemplate.send(Constants.TOPIC_SMART_PDF_BATCH_REQUEST, message);
            return Result.success(true);
        } catch (Exception e) {
            log.error("消息格式错误：{}", e.getMessage(), e);
            return Result.error("消息格式错误：" + e.getMessage());
        }
    }

    @PostMapping("/test")
    public Result<Boolean> test(@RequestBody @Valid ReportMessage request) {
        log.info("收到请求: {}", request.getReqId());
        try {
            reportService.gen(request);
            return Result.success(true);
        } catch (Exception e) {
            log.error("消息格式错误：{}", e.getMessage(), e);
            return Result.error("消息格式错误：" + e.getMessage());
        }
    }

}
