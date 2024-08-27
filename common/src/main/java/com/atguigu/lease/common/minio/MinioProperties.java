package com.atguigu.lease.common.minio;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

//将yml配置内容装配成类
@ConfigurationProperties(prefix = "minio")
//将 "application.yml" 配置文件中的键-值自动映射注入 Java Bean 中，Java bean 的属性必须提供 setter 方法才能注入值。
@Data
public class MinioProperties {
    private String endpoint;
    private String accessKey;
    private String secretKey;
    public String bucketName;
}
