package com.smart.neural.util;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.smart.neural.constant.OssConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class OssUtils implements InitializingBean, DisposableBean {

    private static OSS client;

    private static OssConfig ossConfig;

    @Autowired
    public OssUtils(OssConfig props) {
        ossConfig = props;
    }

    @Override
    public void afterPropertiesSet() {
        client = new OSSClientBuilder().build(
                ossConfig.getEndpoint(),
                ossConfig.getAccessKeyId(),
                ossConfig.getAccessKeySecret()
        );
    }

    @Override
    public void destroy() {
        client.shutdown();
    }

    /**
     * 上传OSS
     *
     * @param path 源文件路径
     * @param key  OSS文件key
     * @param name OSS文件名
     */
    public static void upload(String path, String key, String name) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentEncoding("UTF-8");
        // 使用URL编码
        try {
            String encodedFilename = URLEncoder.encode(name, StandardCharsets.UTF_8.toString());
            metadata.setContentDisposition("attachment;filename=" + encodedFilename);
            client.putObject(ossConfig.getBucketName(), key, new File(path), metadata);
        } catch (UnsupportedEncodingException e) {
            log.error("文件名编码失败：{}", name);
            throw new RuntimeException(e);
        }
    }



}
