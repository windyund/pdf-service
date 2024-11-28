package com.smart.neural.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rholder.retry.Retryer;
import com.github.rholder.retry.RetryerBuilder;
import com.github.rholder.retry.StopStrategies;
import com.github.rholder.retry.WaitStrategies;
import com.smart.neural.constant.Constants;
import com.smart.neural.controller.vo.ReportMessage;
import com.smart.neural.service.ReportService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * 批量处理多条消息，增加并行处理
 */
@Slf4j
@Service
public class BatchReportListener {
    @Resource private ObjectMapper objectMapper;
    @Resource private ReportService reportService;

    private final Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder()
            .retryIfException()
            .withWaitStrategy(WaitStrategies.fixedWait(5, TimeUnit.SECONDS)) // 失败后，隔5秒后重试
            .withStopStrategy(StopStrategies.stopAfterAttempt(3))   //失败重试3次
            .build();

    /**
     * 每次最多处理4条消息
     */
    @KafkaListener(topics = Constants.TOPIC_SMART_PDF_BATCH_REQUEST, containerFactory = "batchFactory")
    public void listen(List<ConsumerRecord<?, ?>> list, Consumer consumer) {
        List<String> messages = new ArrayList<>();
        for (ConsumerRecord<?, ?> record : list) {
            Optional<?> kafkaMessage = Optional.ofNullable(record.value());
            // 获取消息
            kafkaMessage.ifPresent(o -> messages.add(o.toString()));
        }

        try {
            long start = System.currentTimeMillis();
            messages.stream().parallel().forEach(message -> {
                try {
                    ReportMessage msg = objectMapper.readValue(message, ReportMessage.class);
                    retryer.call(() -> reportService.gen(msg));
                } catch (JsonProcessingException e) {
                    log.error("消息格式错误：{}", message, e);
                } catch (Exception e) {
                    log.error("处理消息时发生异常：{}", message, e);
                }
            });
            log.info("消息条数：{}, 批量处理消息耗时: {}ms", messages.size(), System.currentTimeMillis() - start);
            //手动提交offset
            consumer.commitSync();
        } catch (Exception e) {
            log.error("批量处理消息时发生异常：{}", e.getMessage(), e);
        }
    }
}
