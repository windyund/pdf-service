package com.smart.neural.constant;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "oss")
@Getter
@Setter
public class OssConfig {
    private String bucketName;
    private String accessKeyId;
    private String accessKeySecret;
    private String endpoint;
}
