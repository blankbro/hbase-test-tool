package io.github.blankbro.springboothbase.hbase.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Getter
@ConfigurationProperties(prefix = HBaseProperties.CONF_PREFIX)
public class HBaseProperties {
    public static final String CONF_PREFIX = "hbase.conf";

    private Map<String, String> properties = new HashMap<>();

}
