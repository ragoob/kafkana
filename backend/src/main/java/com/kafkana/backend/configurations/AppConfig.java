package com.kafkana.backend.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
public class AppConfig {
    private  kafkaConfig kafka;

    public kafkaConfig getKafka() {
        return kafka;
    }

    public void setKafka(kafkaConfig kafka) {
        this.kafka = kafka;
    }
}
