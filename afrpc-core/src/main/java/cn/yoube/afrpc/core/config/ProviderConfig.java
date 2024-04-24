package cn.yoube.afrpc.core.config;

import cn.yoube.afrpc.core.api.RegistryCenter;
import cn.yoube.afrpc.core.provider.ProviderBootstrap;
import cn.yoube.afrpc.core.provider.ProviderInvoker;
import cn.yoube.afrpc.core.registry.af.AfRegistryCenter;
import cn.yoube.afrpc.core.registry.zk.ZkRegistryCenter;
import cn.yoube.afrpc.core.transport.SpringBootTransport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

/**
 * @author LimMF
 * @since 2024/3/7
 **/
@Configuration
@Import({AppProperties.class, ProviderProperties.class, SpringBootTransport.class})
public class ProviderConfig {

    @Bean
    ProviderBootstrap providerBootstrap() {
        return new ProviderBootstrap();
    }

    @Bean
    ProviderInvoker providerInvoker(@Autowired ProviderBootstrap providerBootstrap) {
        return new ProviderInvoker(providerBootstrap);
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner providerBootstrapRunner(@Autowired ProviderBootstrap providerBootstrap) {
        return x -> {
            providerBootstrap.start();
        };
    }

    @Bean //(initMethod = "start", destroyMethod = "stop")
    public RegistryCenter providerRc() {
        return new AfRegistryCenter();
    }
}
