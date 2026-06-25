package com.ruoyi.flowable.config;

import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.ProcessEngineConfigurationConfigurer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Flowable 8 配置
 *
 * @author aibiz
 */
@Configuration
public class FlowableConfig {

    /**
     * 配置流程引擎，使用MySQL数据库，使用若依数据源
     */
    @Bean
    public ProcessEngineConfigurationConfigurer processEngineConfigurationConfigurer() {
        return engineConfiguration -> {
            engineConfiguration.setActivityFontName("宋体");
            engineConfiguration.setAnnotationFontName("宋体");
            engineConfiguration.setLabelFontName("宋体");
        };
    }
}
