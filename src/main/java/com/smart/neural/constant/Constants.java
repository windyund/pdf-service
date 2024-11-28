package com.smart.neural.constant;

public class Constants {

    // 报告目录
    public static final String REPORT_WORKDIR = "report";

    // kafka topic
    public static final String TOPIC_SMART_PDF_REQUEST = "smart_report_request";
    public static final String TOPIC_SMART_PDF_BATCH_REQUEST = "smart_report_batch_request";
    public static final String TOPIC_SMART_PDF_REALTIME_REQUEST = "smart_report_realtime_request";
    public static final String TOPIC_SMART_PDF_NOTIFY = "smart_report_notify";

    // 报告页面选择器
    public static final String _header_selector = ".pdf-header";
    public static final String _done_selector = "._render_done";
    public static final String _content_selector = ".pdf-container";
    public static final String _page_selector = ".pdf-page";
    // 等待时间不能太长，防止错误的页面占用资源
    public static final Integer WAIT_TIME_MS = 20 * 1000;

}
