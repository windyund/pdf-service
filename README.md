# pdf-service
A PDF generation service developed based on Microsoft's Playwright. It supports generating vector PDFs and generating PDFs by stitching screenshots. It is stable under high concurrency tests.

### 目录结构

```
workspace
└── {req_id_01: 单次请求}
    ├── img
    │   ├── {report_id_01: 报告1}
    │   │   ├── 0.jpeg
    │   │   ├── 1.jpeg
    │   │   └── ...
    │   ├── {report_id_02: 报告2}
    │   │   ├── 0.jpeg
    │   │   ├── 1.jpeg
    │   │   └── ...
    │   └── ...
    │
    └── pdf
        ├── 区级
        │   └── 全区.pdf
        └── 校级
            ├── 学校1.pdf
            ├── 学校2.pdf
            └── ...
```


## 部署

- ci 参考
  ```
  https://playwright.dev/java/docs/ci-intro
  ```
  
## 支持http和kafka 2种方式发送请求服务
请求参数或者消息体
```json5
{
  "req_id": "61d6c859fcca77b5f6a29f6f-12205239", // 请求id,标识一次唯一请求
  "biz_id": "61d6c859fcca77b5f6a29f6f-12205239", // 业务id
  "biz_type": "report",   // 用于业务类型标识，业务回调结果根据该字段可以处理相应逻辑
  "handler": "pdf",  // pdf or screenshot, pdf：生成矢量pdf，用于对网页直接生成， screenshot：对页面有分页要求，要求div添加class属性，分页精准截图
  "reports": [
    {
      "report_id": "61d6c859fcca77b5f6a29f6f-12205239", //pdf id，唯一标识
      "report_name": "赵行成绩单.pdf",           //生成pdf名称
      "report_path": "监测",                   // pdf在oss存放路径
      "oss_key": "xxx.pdf",                   // oss key
      "report_url": "https://xxx"             //可以访问的网页url,如果是非公开页面（eg:需要登录验证的），可以修改添加cookie或者带有签名sign
    }
  ],
  "notify": {                    //生成成功，回调方式，支持kafka和http, http待实现
    "type": "kafka",
    "callback": "smart_pdf_notify"
  }
}
```

