package cn.yoube.afrpc.core.provider;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author LimMF
 * @since 2024/3/7
 **/
@Configuration
public class ProviderConfig {

    @Bean
    ProviderBootstrap providerBootstrap() {
        return new ProviderBootstrap();
    }
}
