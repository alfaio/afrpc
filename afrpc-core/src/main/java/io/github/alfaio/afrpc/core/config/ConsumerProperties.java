package io.github.alfaio.afrpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author LimMF
 * @since 2024/4/16
 **/
@Data
@ConfigurationProperties(prefix = "afrpc.consumer")
public class ConsumerProperties {

    /**
     * 重试次数
     */
    private int retries = 3;
    /**
     * 超时时间
     */
    private int timeout = 1000;
    /**
     * 恢复比例 0-100
     */
    private int grayRatio = 10;

}
