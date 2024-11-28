package com.smart.neural.controller.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
/**
 * PDF请求参数
 */
@Data
public class ReportMessage implements Serializable {
    @Serial private static final long serialVersionUID = -5693752477778742325L;

    /**
     * 请求唯一标识
     */
    @JsonProperty("req_id")
    private String reqId;

    /**
     * 业务ID
     */
    @JsonProperty("biz_id")
    @NotBlank(message = "biz_id不能为空")
    private String bizId;

    /**
     * 业务类型
     */
    @JsonProperty("biz_type")
    @NotBlank(message = "biz_type不能为空")
    private String bizType;

    /**
     * 处理方式：snapshot截图，pdf网页生成pdf
     */
    @JsonProperty("handler")
    private String handler = "snapshot";

    /**
     * 报告参数列表
     */
    @JsonProperty("reports")
    @NotNull(message = "reports不能为空")
    private List<@Valid ReportParams> reports;

    /**
     * 是否打包
     */
    @JsonProperty("zip")
    private boolean zip;

    /**
     * 压缩包名称
     */
    @JsonProperty("zip_name")
    private String zipName;

    /**
     * 压缩包oss路径
     */
    @JsonProperty("zip_oss_key")
    private String zipOssKey;

    /**
     * 回调参数
     */
    @JsonProperty("notify")
    @NotNull(message = "notify不能为空")
    private Notify notify;

    /**
     * 返回结果
     */
    private boolean result;

    /**
     * 返回消息
     */
    private String message;


    @Data
    @AllArgsConstructor
    public static class ReportParams {
        /**
         * 报告唯一标识
         */
        @JsonProperty("report_id")
        @NotBlank(message = "reports.report_id不能为空")
        private String reportId;

        /**
         * 报告名称，oss下载附件时显示
         */
        @JsonProperty("report_name")
        @NotBlank(message = "reports.report_name不能为空")
        private String reportName;

        /**
         * 报告相对路径，打包时zip内路径
         */
        @JsonProperty("report_path")
        @NotBlank(message = "reports.report_path不能为空")
        private String reportPath;

        /**
         * 报告url
         */
        @JsonProperty("report_url")
        @NotBlank(message = "reports.report_url不能为空")
        private String reportUrl;

        /**
         * oss存储路径
         */
        @JsonProperty("oss_key")
        @NotBlank(message = "reports.oss_key不能为空")
        private String ossKey;

        /**
         * 扩展参数
         */
        @JsonProperty("ext")
        private Map<String, Object> ext;

    }


    @Data
    public static class Notify {
        /**
         * 回调类型: http,kafka
         */
        @JsonProperty("type")
        @NotBlank(message = "notify.type不能为空")
        private String type;

        /**
         * 回调url，或者topic
         */
        @JsonProperty("callback")
        private String callback;
    }
}
