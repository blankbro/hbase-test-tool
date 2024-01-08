package io.github.blankbro.springboothbase.hbase.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(HBaseProperties.class)
public class HBaseAutoConfiguration {
}
