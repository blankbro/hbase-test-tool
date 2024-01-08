package io.github.blankbro.springboothbase.hbase.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = HBaseConfig.CONF_PREFIX)
public class HBaseConfig {
    public static final String CONF_PREFIX = "hbase.conf";

    private Map<String, String> confMaps;

    public Map<String, String> getConfMaps() {
        return confMaps;
    }

    public void setConfMaps(Map<String, String> confMaps) {
        this.confMaps = confMaps;
    }
}
