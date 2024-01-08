package io.github.blankbro.springboothbase.hbase.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Getter
@Configuration
@ConfigurationProperties(prefix = HBaseConfig.CONF_PREFIX)
public class HBaseConfig {
    public static final String CONF_PREFIX = "hbase.conf";

    private Map<String, String> properties;

}
