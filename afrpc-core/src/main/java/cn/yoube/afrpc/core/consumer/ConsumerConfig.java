package cn.yoube.afrpc.core.consumer;

import cn.yoube.afrpc.core.api.LoadBalancer;
import cn.yoube.afrpc.core.api.RegistryCenter;
import cn.yoube.afrpc.core.api.Router;
import cn.yoube.afrpc.core.cluster.RoundRibonLoadBalancer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

/**
 * @author LimMF
 * @since 2024/3/10
 **/
@Configuration
public class ConsumerConfig {

    @Value("${afrpc.providers}")
    String servers;

    @Bean
    ConsumerBootstrap consumerBootstrap() {
        return new ConsumerBootstrap();
    }

    @Bean
    @Order(Integer.MIN_VALUE)
    public ApplicationRunner consumer_Runner(@Autowired ConsumerBootstrap consumerBootstrap) {
        return x -> {
            consumerBootstrap.start();
        };
    }

    @Bean
    public LoadBalancer loadBalancer() {
        return new RoundRibonLoadBalancer();
    }

    @Bean
    public Router router() {
        return Router.Default;
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public RegistryCenter.StaticRegistryCenter staticRegistryCenter() {
        return new RegistryCenter.StaticRegistryCenter(List.of(servers.split(",")));
    }

}
