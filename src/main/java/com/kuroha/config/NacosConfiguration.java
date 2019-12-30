package com.kuroha.config;

import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.annotation.NacosProperties;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.spring.context.annotation.discovery.EnableNacosDiscovery;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @author kuroha
 */
@Configuration
public class NacosConfiguration {

    @Value("${nacos.server-addr}")
    private String nacosServerAddr;

    /**
     * naming服务
     * @return
     */
    @Bean
    public NamingService namingService() {
        try {
            Properties properties = new Properties();
            properties.put("serverAddr",nacosServerAddr);
            return NacosFactory.createNamingService(properties);
        } catch (NacosException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 配置服务
     * @return
     */
    @Bean
    public ConfigService configService() {
        try {
            Properties properties = new Properties();
            properties.put("serverAddr",nacosServerAddr);
            ConfigService configService = NacosFactory.createConfigService(properties);
            String config = configService.getConfig("spring-nacos.properties", "DEFAULT_GROUP", 2000);
            // 为可用properties,如果有需要可以进行改造,创建一个新的类,仿造Spring的注入方法进行属性注入
            properties.load(new ByteArrayInputStream(config.getBytes(StandardCharsets.UTF_8)));
            return configService;
        }catch (NacosException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
