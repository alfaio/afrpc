package io.github.alfaio.afrpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author LimMF
 * @since 2024/4/16
 **/
@Data
@ConfigurationProperties(prefix = "afrpc.app")
public class AppProperties {

    /**
     * 应用标识
     */
    private String id = "app1";
    /**
     * 命名空间
     */
    private String namespace = "public";
    /**
     * 环境
     */
    private String env = "dev";
}
