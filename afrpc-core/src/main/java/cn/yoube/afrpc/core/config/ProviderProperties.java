package cn.yoube.afrpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * @author LimMF
 * @since 2024/4/16
 **/
@Data
@ConfigurationProperties(prefix = "afrpc.provider")
public class ProviderProperties {

    /**
     * 元数据
     */
    private Map<String, String> metas = new HashMap<>();

}
